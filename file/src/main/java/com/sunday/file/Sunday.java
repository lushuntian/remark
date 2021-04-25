package com.sunday.file;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 我的日期时间工具类
 * @author sunday
 */
public final class Sunday {
    private final static DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 获取当前日期，如：20210414
     */
    public final static String getDate(){
        LocalDateTime date = LocalDateTime.now();
        return date.format(format1);
    }

}
