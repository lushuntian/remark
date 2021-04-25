package com.sunday.remark.config;

import net.sf.json.JSONArray;

/**
 * 系统常量
 */
public class SystemConstants {
    /**
     * 评价查询条目
     */
    public final static int REMARK_MAX_LIST_LENGTH = 10;

    /**
     * 评价保留数量
     */
    public final static int REMARK_MAX_DISPLAY_LENGTH = 50;

    /**
     * 评价推荐数量
     */
    public final static int REMARK_MAX_RECOMMEND_LENGTH = 100;

    /**
     * 空Json数组
     */
    public final static JSONArray EMPTY_ARRAY = new JSONArray();

    /**
     * 投票topic
     */
    public final static String VOTE_TOPIC = "Topic_Vote";

}
