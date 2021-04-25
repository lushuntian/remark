package com.sunday.remark.service.util;

import org.springframework.util.StringUtils;

/**
 * 通用处理
 */
public class CommonUtil {
    /**
     * 融合多个字符串拼接成新的字符串，字符串之间点号分割
     */
    public static String merge(Object... strings){
        if (strings == null || strings.length == 0)
            return null;

        if (strings.length < 4){
            String str = String.valueOf(strings[0]);
            for (int i = 1; i < strings.length; i++) {
                str += "." + strings[i];
            }
            return str;
        }

        StringBuilder sb = new StringBuilder(String.valueOf(strings[0]));
        for (int i = 1; i < strings.length; i++) {
            sb.append(".").append(strings[i]);
        }
        return sb.toString();
    }
}
