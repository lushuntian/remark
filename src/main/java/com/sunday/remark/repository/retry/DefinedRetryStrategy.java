package com.sunday.remark.repository.retry;

/**
 * 指定间隔时间的重试策略
 */
public class DefinedRetryStrategy implements IRetryStrategy {
    private final int[] intervals;

    public DefinedRetryStrategy(int... intervals) {
        if (intervals.length == 0) {
            this.intervals = new int[]{0};
        } else {
            this.intervals = intervals;
        }
    }

    private DefinedRetryStrategy() {
        this.intervals = new int[]{0};
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
        return System.currentTimeMillis() > lastTimestamp + getWaitSecond(attemptTimes) * 1000L;
    }

    @Override
    public void noticeFail(int itemId) {

    }

    @Override
    public void noticeSuccess(int itemId) {

    }

    /**
     * 根据当前重试次数，获取下一次重试等待间隔（单位：秒）
     */
    private int getWaitSecond(int attemptTimes) {
        if (attemptTimes < 0) {
            attemptTimes = 0;
        }

        if (attemptTimes >= intervals.length) {
            attemptTimes = intervals.length - 1;
        }

        return intervals[attemptTimes];
    }
}






