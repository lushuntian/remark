package com.sunday.remark.repository.retry;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 熔断器抽象类
 */
public abstract class AbstractCircuitBreaker implements CircuitBreaker{
    //熔断恢复时间
    private final static long RECOVERY_TIMEOUT_MS;

    //下一次恢复时间
    private volatile long nextRetryTimestamp;

    //当前状态
    protected final AtomicReference<State> currentState = new AtomicReference<>(State.CLOSED);

    static {
        RECOVERY_TIMEOUT_MS = 60_000;
    }

    @Override
    public State currentState() {
        return currentState.get();
    }

    @Override
    public boolean canPass() {
        if (currentState.get() == State.CLOSED) {
            return true;
        }

        if (currentState.get() == State.OPEN) {
            // For half-open state we allow a request for probing.
            return retryTimeoutArrived() && fromOpenToHalfOpen();
        }

        return true;
    }

    protected boolean retryTimeoutArrived() {
        return System.currentTimeMillis() >= nextRetryTimestamp;
    }

    protected void updateNextRetryTimestamp() {
        this.nextRetryTimestamp = System.currentTimeMillis() + RECOVERY_TIMEOUT_MS;
    }

    protected void fromCloseToOpen() {
        if (currentState.compareAndSet(State.CLOSED, State.OPEN)) {
            updateNextRetryTimestamp();
        }
    }

    protected boolean fromOpenToHalfOpen() {
        return currentState.compareAndSet(State.OPEN, State.HALF_OPEN);
    }

    protected void fromHalfOpenToOpen() {
        if (currentState.compareAndSet(State.HALF_OPEN, State.OPEN)) {
            updateNextRetryTimestamp();
        }
    }

    protected void fromHalfOpenToClose() {
        currentState.compareAndSet(State.HALF_OPEN, State.CLOSED);
    }

    /**
     * Reset the statistic data.
     */
    protected abstract void resetStat();

}