package com.sunday.remark.repository.retry;

/**
 * 断路器
 */
public interface CircuitBreaker {
    /**
     * Acquires permission of an invocation only if it is available at the time of invoking.
     *
     * @return {@code true} if permission was acquired and {@code false} otherwise
     */
    boolean canPass();

    /**
     * Get current state of the circuit breaker.
     *
     * @return current state of the circuit breaker
     */
    State currentState();

    /**
     * Record a success request with the context and handle state transformation of the circuit breaker.
     *
     */
    void onSuccess();

    /**
     * Record a fail request with the context and handle state transformation of the circuit breaker.
     *
     */
    void onFail();

    /**
     * Circuit breaker state.
     */
    enum State {
        /**
         * 断路器开，所有的请求被中断
         */
        OPEN,
        /**
         * 断路器半开，不稳定状态，根据下一请求的执行结果决定最终状态
         */
        HALF_OPEN,
        /**
         * 断路器闭合，所有的请求允许通过
         */
        CLOSED
    }
}
