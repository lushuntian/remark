package com.sunday.remark.repository.retry;


/**
 * 重试任务.
 * 非线程安全
 */
public abstract class AbstractRetryTask<V> implements IRetryTask<V> {
    //重试等待间隔
    protected final IRetryStrategy retryStrategy;

    //当前重试次数
    protected int curAttemptTimes = -1;

    //最大重试次数
    private final int maxAttemptTimes;

    //上一次重试的时间戳
    protected long lastTimestamp = 0;

    public AbstractRetryTask(IRetryStrategy retryStrategy, int maxAttemptTimes) {
        this.retryStrategy = retryStrategy;
        this.maxAttemptTimes = maxAttemptTimes;
    }

    /**
     * 执行一次重试
     *
     * @return 如果执行成功，返回true，否则返回false
     */
    @Override
    public boolean tryOnce() {
        if (isFinished()) {
            return true;
        }

        setNextCycle();

        //执行重试
        doTry();

        //重试任务执行异常或者返回null，将视为执行失败
        return isFinished();
    }

    /**
     * 是否结束
     */
    protected abstract boolean isFinished();

    /**
     * 执行回调
     */
    protected abstract void doTry();

    /**
     * 是否应该关闭任务
     *
     * @return 如果达到最大重试次数，返回true，表示可以关闭
     */
    @Override
    public boolean shouldClose() {
        return curAttemptTimes >= maxAttemptTimes;
    }

    //设置下一执行周期
    private void setNextCycle() {
        curAttemptTimes++;
        lastTimestamp = System.currentTimeMillis();
    }
}
