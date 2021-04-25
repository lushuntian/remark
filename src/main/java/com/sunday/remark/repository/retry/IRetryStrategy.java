package com.sunday.remark.repository.retry;

/**
 * 重试策略，决定任务何时可以重试
 */
public interface IRetryStrategy {

    /**
     * 现在是否应该执行重试
     * @param attemptTimes 第几次重试
     * @param lastTimestamp 上一次重试的时间戳
     * @param itemId 当前的执行项目id
     * @return 允许重试，返回true，否则，返回false
     */
    boolean shouldTryAtNow(int attemptTimes, long lastTimestamp, int itemId);

    /**
     * 通知一次失败
     * @param itemId 当前的执行项目id
     */
    void noticeFail(int itemId);

    /**
     * 通知一次成功
     * @param itemId 当前的执行项目id
     */
    void noticeSuccess(int itemId);
}
