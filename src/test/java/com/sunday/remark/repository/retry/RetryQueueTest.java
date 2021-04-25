package com.sunday.remark.repository.retry;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class RetryQueueTest {
    private final static int NUM = 100000;
    private List<String> messages1 = Collections.synchronizedList(new ArrayList<>());


    IRetryTaskFactory taskFactory = new IRetryTaskFactory() {
        @Override
        public <V> IRetryTask createRetryTask(List<Repeatable<V>> segments) {
            return new SegmentRetryTask<>(new DefinedRetryStrategy(0), 10, segments);
        }
    };

    RetryQueue retryQueue = new RetryQueue(taskFactory);

    @Test
    void submit() {
        List<Repeatable<String>> list = new ArrayList<>();
        list.add(new Executable<>() {
            @Override
            public void execute(int repeatTimes) throws Exception {
                if (repeatTimes < 4)
                    throw new Exception();
                messages1.add("good");
            }
        });

        //模拟高并发提交
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        Semaphore semaphore = new Semaphore(0);
        for (int i = 0; i < NUM; i++) {
            executorService.submit(() -> {
                try {
                    retryQueue.submit(list);
                } catch (RetryRefuseException e) {
                    fail();
                }
                semaphore.release();
            });
        }

        executorService.shutdown();

        //等待执行完成
        try {
            semaphore.acquire(NUM);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //等待执行完成
        while (!retryQueue.isEmpty()) Thread.yield();


        assertEquals(NUM, messages1.size());
        for (String s : messages1) {
            assertEquals(s, "good");
        }
    }
}