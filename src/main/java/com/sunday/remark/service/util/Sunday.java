package com.sunday.remark.service.util;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 我的日期时间工具类
 * @author sunday
 */
public final class Sunday {
    private final static DateTimeFormatter format1 = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final static DateTimeFormatter format2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前日期，如：20210414
     */
    public final static String getDate(){
        LocalDateTime date = LocalDateTime.now();
        return date.format(format1);
    }

    /**
     * 获取当前日期，如：2021-04-08 09:46:36
     */
    public final static String getTime(){
        LocalDateTime date = LocalDateTime.now();
        return date.format(format2);
    }

}
