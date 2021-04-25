package com.sunday.remark.repository.retry;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.CircuitBreakingException;

import java.util.List;

/**
 * 多段重试任务. 任务链路执行失败时，下一次重试从失败的点继续执行。
 */
@Slf4j
public class SegmentRetryTask<V> extends AbstractRetryTask<V> {
    //分段执行方法
    private final List<Repeatable<V>> segments;

    //当前执行片段，上一次执行中断的片段
    private int currentSegment = 0;

    //上一次的执行结果值
    private V result;

    public SegmentRetryTask(IRetryStrategy retryStrategy, int maxAttemptTimes, List<Repeatable<V>> segments) {
        super(retryStrategy == null ? new DefinedRetryStrategy(0) : retryStrategy, maxAttemptTimes);
        this.segments = segments;
    }

    /**
     * 执行回调
     */
    @Override
    protected void doTry() {
        try {
            for (; currentSegment < segments.size(); currentSegment++) {
                //如果当前断路器打开，不尝试执行
                if (retryStrategy instanceof SmartRetryStrategy){
                    if (!((SmartRetryStrategy)retryStrategy).canPass(currentSegment)) {
                        segments.get(currentSegment).recordFail(curAttemptTimes, new CircuitBreakingException());
                        return;
                    }
                }

                //如果抛异常，分段计数器不增加，下次从这个地方执行
                Repeatable<V> repeatable = segments.get(currentSegment);
                if (!execute(repeatable)) return;
            }
        } catch (Exception e) {
            retryStrategy.noticeFail(currentSegment);
            if (currentSegment < segments.size()) {
                if (shouldClose()) {
                    segments.get(currentSegment).recordEnd(e);
                } else {
                    segments.get(currentSegment).recordFail(curAttemptTimes, e);
                }
            }
        }
    }

    private boolean execute(Repeatable<V> repeatable) throws Exception {
        if (repeatable instanceof Computable) {
            result = repeatable.compute(curAttemptTimes);
            if (result == null) {
                repeatable.recordFail(curAttemptTimes);
                retryStrategy.noticeFail(currentSegment);
                return false;
            }
            retryStrategy.noticeSuccess(currentSegment);
        }

        if (repeatable instanceof Executable) {
            if (result == null) {
                repeatable.execute(curAttemptTimes);
            } else {
                repeatable.execute(curAttemptTimes, result);
            }
            retryStrategy.noticeSuccess(currentSegment);
        }
        return true;
    }

    @Override
    protected boolean isFinished() {
        return currentSegment >= segments.size();
    }

    /**
     * 现在是否应该执行重试
     *
     * @return 当等待时间超过重试间隔时间后，允许重试，返回true，否则，返回false
     */
    @Override
    public boolean shouldTryAtNow() {
        return retryStrategy.shouldTryAtNow(curAttemptTimes, lastTimestamp, currentSegment);
    }

    /**
     * 获取执行结果
     */
    @Override
    public V getResult() {
        return result;
    }
}
