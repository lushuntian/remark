package com.sunday.remark.repository.retry;

public class RetryRefuseException extends Exception {
    private final static RetryRefuseException instance = new RetryRefuseException("超过重试队列容量！");

    public RetryRefuseException() {
        super();
    }

    public RetryRefuseException(String message) {
        super(message);
    }

    public RetryRefuseException(String message, Throwable cause) {
        super(message, cause);
    }

    public static RetryRefuseException getInstance() {
        return instance;
    }
}
