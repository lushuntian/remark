package com.sunday.remark.repository.retry;

/**
 * 可重复执行的任务
 */
public interface Repeatable<V> extends IFailRecorder{
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @param repeatTimes repeat times, first repeatTimes is 0
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    V compute(int repeatTimes) throws Exception;

    /**
     * Execute with no result, and throws an exception if unable to do so.
     *
     * @param repeatTimes repeat times, first repeatTimes is 0
     * @param receiveValue last step computed result
     * @throws Exception if unable to compute a result
     */
    default void execute(int repeatTimes, V receiveValue) throws Exception{}

    /**
     * Execute with no result, and throws an exception if unable to do so.
     *
     * @param repeatTimes repeat times, first repeatTimes is 0
     * @throws Exception if unable to compute a result
     */
    default void execute(int repeatTimes) throws Exception{}
}









