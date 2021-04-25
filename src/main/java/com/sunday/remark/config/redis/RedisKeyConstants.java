package com.sunday.remark.config.redis;

public class RedisKeyConstants {
    /**
     * 评价列表前缀
     */
    public final static String REMARK_PREFIX = "remarks:";

    /**
     * 评价代理键前缀
     */
    public final static String REMARK_PROXY_PREFIX = "remark.b:";

    /**
     * 加锁前缀 {0}:用途   {1}:id
     */
    public final static String LOCK_PATTERN = "lock:{0}:{1}";

    /**
     * ”用户点赞内容“ 的KEY模式
     */
    public final static String VOTE_USER_PATTERN = "vote:{0}:{1}";

    /**
     * ”内容点赞总数“ 的KEY模式
     */
    public final static String VOTE_SUM_PATTERN = "votes:{0}";

    /**
     * 评价评分前缀
     */
    public final static String REMARK_SCORE_PREFIX = "remark.s:";

    /**
     * 评价评分锁前缀
     */
    public final static String REMARK_SCORE_LOCK_PREFIX = "remark.s.l:";

    /**
     * 评价推荐前缀
     */
    public final static String REMARK_RECOMMEND_PREFIX = "remark.r:";

    /**
     * 评价推荐代理键前缀
     */
    public final static String REMARK_RECOMMEND_PROXY_PREFIX = "remark.r.b:";

}
