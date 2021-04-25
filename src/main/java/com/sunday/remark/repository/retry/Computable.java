package com.sunday.remark.repository.retry;

/**
 * 可计算任务
 * @param <V> 计算结果类型
 */
public abstract class Computable<V> implements Repeatable<V>{
    /**
     * Execute with no result, and throws an exception if unable to do so.
     *
     * @param repeatTimes repeat times
     * @throws Exception if unable to compute a result
     */
    @Override
    public void execute(int repeatTimes) throws Exception {
        throw new IllegalAccessException("不支持的方法");
    }

    @Override
    public void execute(int repeatTimes, V receiveValue) throws Exception {
        throw new IllegalAccessException("不支持的方法");
    }
}
