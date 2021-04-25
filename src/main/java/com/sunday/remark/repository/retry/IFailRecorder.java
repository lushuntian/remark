package com.sunday.remark.repository.retry;

/**
 * 失败记录器
 */
interface IFailRecorder {
    /**
     * 记录每次重试的失败情况
     * @param attemptTimes 重试次数，第一次重试=0
     * @param e 导致失败的异常
     */
    default void recordFail(int attemptTimes, Exception e){}

    /**
     * 记录每次重试的失败情况
     * @param attemptTimes 重试次数，第一次重试=0
     */
    default void recordFail(int attemptTimes){}

    /**
     * 记录重试之后的最终失败
     * @param e 导致失败的异常，如果没有异常，返回null
     */
    default void recordEnd(Exception e){}
}
