package com.sunday.remark.repository.retry;

/**
 * 可执行任务
 */
public abstract class Executable<V> implements Repeatable<V>{
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @param repeatTimes repeat times
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public V compute(int repeatTimes) throws Exception {
        throw new IllegalAccessException("不支持的方法");
    }
}
