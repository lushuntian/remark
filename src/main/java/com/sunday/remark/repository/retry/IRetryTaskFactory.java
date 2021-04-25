package com.sunday.remark.repository.retry;

import java.util.List;

/**
 * 线程安全的“重试任务”工厂.
 */
public interface IRetryTaskFactory {
    /**
     * 创建重试任务
     * @param segments 分段执行任务
     * @return 重试任务
     */
    <V> IRetryTask<V> createRetryTask(List<Repeatable<V>> segments);
}
