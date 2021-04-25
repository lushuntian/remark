package com.sunday.remark.repository.retry;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;

/**
 * 线程安全的重试队列。
 * (Spring-retry 和 guava-retrying都不完全适合这个场景，决定自己开发一个简单的重试机制)
 * 重试队列会尽最大努力让任务多次执行并成功，使用时需要考虑以下几点。
 * 1.重试队列存储在内存之中，暂未同步到磁盘，要求使用者可以承受丢失的风险。
 * 2.重试不保证一定会成功，它将在重试一定的次数后结束，如果最终失败，将记录失败结果。
 * 3.为了不让频繁的重试让系统的负载过大，建议设置恰当的重试间隔，以起到削峰填谷的作用。
 * 4.当超过重试队列允许容纳的数量时，将抛出异常。
 * 5.重试任务将在独立的线程中执行，不会阻塞当前线程
 * 6.重试任务执行异常或者返回null，将视为执行失败。暂不支持拦截自定义异常。
 * 7.由于网络问题，远程过程执行成功未必代表会返回成功，重试任务需要实现幂等性。
 * 8."队列"仅指按先进先出的顺序扫描任务，任务移除队列操作取决于其何时完成或结束
 *
 * @author sunday
 * @version 0.0.1
 */
public final class RetryQueue {
    //重试任务队列(全局唯一)
    private final static Deque<IRetryTask> retryTaskList = new ConcurrentLinkedDeque<>();

    //重试任务工厂
    private final IRetryTaskFactory retryTaskFactory;

    public RetryQueue(IRetryTaskFactory retryTaskFactory) {
        this.retryTaskFactory = retryTaskFactory;
    }

    static {
        Thread daemon = new Thread(RetryQueue::scan);
        daemon.setDaemon(true);
        daemon.setName(RetryConstants.RETRY_THREAD_NAME);
        daemon.start();
    }

    //扫描重试队列，执行重试并移除任务（如果成功），周期性执行
    private static void scan() {
        while (true) {
            //先执行，再删除
            retryTaskList.removeIf(task -> retry(task) || task.shouldClose());

            // wait some times
            try {
                TimeUnit.MILLISECONDS.sleep(RetryConstants.SCAN_INTERVAL);
            } catch (Throwable ignored) {
            }
        }
    }

    //执行重试
    private static boolean retry(/*not null*/IRetryTask task) {
        if (task.shouldTryAtNow()) {
            return task.tryOnce();
        }
        return false;
    }

    /**
     * 提交任务。在当前线程立刻执行，如果失败，则使用设置的重试任务工厂创建包装对象，把这个对象写入重试队列等待异步重试。
     *
     * @param segments 分段执行任务
     * @param <V>      结果返回类型
     * @return 如果当前线程一次就执行成功，同步返回结果值，否则加入重试队列，异步通知结果值。
     * @throws RetryRefuseException 当超过重试队列允许容纳的数量时，将抛出异常
     */
    public final <V> V submit(List<Repeatable<V>> segments) throws RetryRefuseException {
        if (segments == null || segments.size() == 0) {
            return null;
        }

        IRetryTask<V> task = retryTaskFactory.createRetryTask(segments);

        //在当前线程执行
        if(!task.tryOnce()){
            //失败后加入队列
            ensureCapacity();
            retryTaskList.push(task);
        }

        //只要当前已经有执行结果，就返回，即便是加入了重试队列
        return task.getResult();
    }

    /**
     * 提交任务。在当前线程立刻执行，如果失败，则使用设置的重试任务工厂创建包装对象，把这个对象写入重试队列等待异步重试。
     *
     * @param repeatable 执行任务
     * @param <V>        结果返回类型
     * @return 如果当前线程一次就执行成功，同步返回结果值，否则加入重试队列，异步通知结果值。
     * @throws RetryRefuseException 当超过重试队列允许容纳的数量时，将抛出异常
     */
    public final <V> V submit(Repeatable<V> repeatable) throws RetryRefuseException {
        return submit(List.of(repeatable));
    }

    //确保容量
    private void ensureCapacity() throws RetryRefuseException {
        //非线程安全，高并发下可能短暂冲破最大容量，不过问题不大
        if (retryTaskList.size() >= RetryConstants.MAX_QUEUE_SIZE) {
            throw RetryRefuseException.getInstance();
        }
    }

    /**
     * 队列是否为空
     *
     * @return 如果当前无正在执行的任务，返回true
     */
    public boolean isEmpty() {
        return retryTaskList.isEmpty();
    }
}















