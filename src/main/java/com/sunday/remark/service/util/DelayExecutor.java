package com.sunday.remark.service.util;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 延迟队列
 */
public final class DelayExecutor {
    private final static Deque<DelayObject> DELAY_QUEUE = new ConcurrentLinkedDeque<>();

    private final static int MAX_SIZE = 1000_000;

    static {
        Thread daemon = new Thread(DelayExecutor::scan);
        daemon.setDaemon(true);
        daemon.setName("DelayExecutor");
        daemon.start();
    }

    /**
     * 等待ms之后执行runnable，等待过程和执行过长是非阻塞的
     *
     * @param delayMillisecond 延迟毫秒数
     */
    public static void executeLater(long delayMillisecond, Runnable runnable) {
        if (DELAY_QUEUE.size() > MAX_SIZE || delayMillisecond <= 0)
            runnable.run();
        else
            DELAY_QUEUE.push(new DelayObject(System.currentTimeMillis() + delayMillisecond, runnable));
    }

    private static void scan() {
        while (true) {
            DELAY_QUEUE.forEach(delayObject -> {
                if (delayObject.reachTime()) {
                    try {
                        delayObject.runnable.run();
                    } catch (Exception ignored) {
                    }
                    DELAY_QUEUE.remove(delayObject);
                }
            });

            try {
                //每隔10ms一次扫描
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static class DelayObject {
        private final long delayTimestamp;
        private final Runnable runnable;

        public DelayObject(long delayTimestamp, Runnable runnable) {
            this.delayTimestamp = delayTimestamp;
            this.runnable = runnable;
        }

        //是否抵达等待时间
        public boolean reachTime() {
            return System.currentTimeMillis() > delayTimestamp;
        }
    }
}
