package com.sunday.remark.repository.retry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 断路器模式实现的智能的重试策略
 */
public class SmartRetryStrategy extends DefinedRetryStrategy {
    //断路器集合
    private final Map<Integer, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    private final Object LOCK = new Object();

    private static CircuitBreaker newCircuitBreaker() {
        return new ExceptionCircuitBreaker();
    }

    public SmartRetryStrategy(int[] intervals) {
        super(intervals);
    }

    private CircuitBreaker getCircuitBreaker(Integer itemId) {
        if (!circuitBreakers.containsKey(itemId)) {
            synchronized (LOCK) {
                if (!circuitBreakers.containsKey(itemId)) {
                    circuitBreakers.put(itemId, newCircuitBreaker());
                }
            }
        }

        return circuitBreakers.get(itemId);
    }

    /**
     * 现在是否应该执行重试
     *
     * @param attemptTimes  第几次重试
     * @param lastTimestamp 上一次重试的时间戳
     * @param itemId        当前的执行项目id
     * @return 允许重试，返回true，否则，返回false
     */
    @Override
    public boolean shouldTryAtNow(int attemptTimes, long lastTimestamp, int itemId) {
        //如果基本条件不满足，则不能重试
        if (!super.shouldTryAtNow(attemptTimes, lastTimestamp, itemId)) {
            return false;
        }

        //断路器是否允许请求通过
        return canPass(itemId);
    }

    /**
     * 通知一次失败
     *
     * @param itemId 当前的执行项目id
     */
    @Override
    public void noticeFail(int itemId) {
        getCircuitBreaker(itemId).onFail();
    }

    /**
     * 通知一次成功
     *
     * @param itemId 当前的执行项目id
     */
    @Override
    public void noticeSuccess(int itemId) {
        getCircuitBreaker(itemId).onSuccess();
    }

    /**
     * 是否允许通过
     */
    public boolean canPass(int itemId){
        return getCircuitBreaker(itemId).canPass();
    }
}
