package com.sunday.remark.service.util;

import org.springframework.util.StringUtils;
import java.util.concurrent.*;

/**
 * 线程安全的异步执行队列
 */
public final class RemarkQueue {
    //线程数
    private final int nThreads;

    //阻塞队列长度
    private final static int BLOCK_LENGTH = 1000_000;

    /*
     * 线程池组，每个线程池只有一个线程。
     * 不适用线程池自带的负载均衡策略，而是使用自制的根据项目id的哈希负载均衡策略
     * 为了降低锁冲突概率。
     */
    private final ExecutorService[] threads;

    public RemarkQueue(int nThreads) {
        this.nThreads = nThreads;
        this.threads = new ExecutorService[nThreads];

        for (int i = 0; i < nThreads; i++) {
            int finalI = i;
            this.threads[i] = new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(BLOCK_LENGTH),
                    r -> new Thread(r, "RemarkQueue:" + finalI),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }
    }

    /**
     * 加入异步执行队列
     * @param itemId 商品id
     */
    public void push(String itemId, Runnable runnable){
        if (StringUtils.isEmpty(itemId)){
            return;
        }

        //根据商品id决定使用哪个线程
        int slot = Math.abs(itemId.hashCode() % nThreads);
        threads[slot].execute(runnable);
    }
}



