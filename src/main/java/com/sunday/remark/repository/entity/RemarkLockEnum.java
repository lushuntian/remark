package com.sunday.remark.repository.entity;

public enum RemarkLockEnum {
    /**
     * 遗弃
     */
    ABORT,

    /**
     * 添加成功
     */
    SUCCESS,

    /**
     * 加锁成功
     */
    LOCK_OK,

    /**
     * 加锁失败，需要重试
     */
    REDO,

    /**
     * 执行错误
     */
    ERROR,
}
