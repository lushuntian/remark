package com.sunday.remark.repository.retry;

import com.alibaba.csp.sentinel.slots.statistic.base.LeapArray;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * 基于异常比率的断路器
 */
public class ExceptionCircuitBreaker extends AbstractCircuitBreaker {
    //触发断路器的最小请求数量
    private final static int MIN_REQUEST_AMOUNT;

    //异常比率
    private final static double THRESHOLD;

    private final LeapArray<SimpleErrorCounter> stat = new SimpleErrorCounterLeapArray(5, 5000);

    static {
        MIN_REQUEST_AMOUNT = 5;
        THRESHOLD = 0.5;
    }

    @Override
    protected void resetStat() {
        stat.currentWindow().value().reset();
    }

    private void handleStateChange(boolean isSuccess) {
        if (currentState.get() == State.OPEN) {
            return;
        }

        //半开状态的切换
        if (currentState.get() == State.HALF_OPEN) {
            // In detecting request
            if (isSuccess) {
                fromHalfOpenToClose();
            } else {
                fromHalfOpenToOpen();
            }
            return;
        }

        List<SimpleErrorCounter> counters = stat.values();
        long errCount = 0;
        long totalCount = 0;
        for (SimpleErrorCounter counter : counters) {
            errCount += counter.errorCount.sum();
            totalCount += counter.totalCount.sum();
        }

        if (totalCount < MIN_REQUEST_AMOUNT) {
            return;
        }

        double curCount = errCount * 1.0d / totalCount;
        if (curCount > THRESHOLD) {
            fromCloseToOpen();
        }
    }

    /**
     * Record a success request with the context and handle state transformation of the circuit breaker.
     */
    @Override
    public void onSuccess() {
        stat.currentWindow().value().getTotalCount().add(1);
        handleStateChange(true);
    }

    /**
     * Record a fail request with the context and handle state transformation of the circuit breaker.
     */
    @Override
    public void onFail() {
        stat.currentWindow().value().getErrorCount().add(1);
        stat.currentWindow().value().getTotalCount().add(1);
        handleStateChange(false);
    }

    static class SimpleErrorCounterLeapArray extends LeapArray<SimpleErrorCounter> {

        public SimpleErrorCounterLeapArray(int sampleCount, int intervalInMs) {
            super(sampleCount, intervalInMs);
        }

        @Override
        public SimpleErrorCounter newEmptyBucket(long timeMillis) {
            return new SimpleErrorCounter();
        }

        @Override
        protected WindowWrap<SimpleErrorCounter> resetWindowTo(WindowWrap<SimpleErrorCounter> w, long startTime) {
            // Update the start time and reset value.
            w.resetTo(startTime);
            w.value().reset();
            return w;
        }
    }

    static class SimpleErrorCounter {
        private final LongAdder errorCount;
        private final LongAdder totalCount;

        public SimpleErrorCounter() {
            this.errorCount = new LongAdder();
            this.totalCount = new LongAdder();
        }

        public LongAdder getErrorCount() {
            return errorCount;
        }

        public LongAdder getTotalCount() {
            return totalCount;
        }

        public void reset() {
            errorCount.reset();
            totalCount.reset();
        }

        @Override
        public String toString() {
            return "SimpleErrorCounter{" +
                    "errorCount=" + errorCount +
                    ", totalCount=" + totalCount +
                    '}';
        }
    }

}
