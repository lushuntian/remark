package com.sunday.remark.repository.retry;

/**
 * 重试任务
 */
public interface IRetryTask<V> {
    /**
     * 执行一次重试
     * @return 如果执行成功，返回true，否则返回false
     */
    boolean tryOnce();

    /**
     * 是否应该关闭任务
     * @return 如果达到最大重试次数，返回true，表示可以关闭
     */
    boolean shouldClose();

    /**
     * 现在是否应该执行重试
     * @return 当等待时间超过重试间隔时间后，允许重试，返回true，否则，返回false
     */
    boolean shouldTryAtNow();

    /**
     * 获取执行结果
     */
    V getResult();
}
