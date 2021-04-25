package com.sunday.remark.repository.retry;

public class RetryConstants {
    //重试任务队列最大允许容量
    public final static int MAX_QUEUE_SIZE = 1_000_000;

    //重试线程名称
    public final static String RETRY_THREAD_NAME = "retryQueue-scan-and-retry-thread";

    //扫描间隔
    public final static int SCAN_INTERVAL = 1_000;

}
