package com.sunday.remark.repository;

import com.sunday.remark.config.redis.RedisKeyConstants;
import com.sunday.remark.repository.entity.Remark;
import com.sunday.remark.repository.entity.RemarkLockEnum;
import com.sunday.remark.service.util.Sunday;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;

/**
 * 评价redis缓存
 */
@Repository
public class RemarkRedis {
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> pushAndLockScript;
    private final DefaultRedisScript<Long> readAndLockScript;
    private final DefaultRedisScript<Object> swapScript;
    private final DefaultRedisScript<Boolean> resetListScript;

    public RemarkRedis(RedisTemplate<String, Object> redisTemplate, DefaultRedisScript<Long> pushAndLockScript, DefaultRedisScript<Long> readAndLockScript, DefaultRedisScript<Object> swapScript, DefaultRedisScript<Boolean> resetListScript) {
        this.redisTemplate = redisTemplate;
        this.pushAndLockScript = pushAndLockScript;
        this.readAndLockScript = readAndLockScript;
        this.swapScript = swapScript;
        this.resetListScript = resetListScript;
    }

    /**
     * 添加评价，并获取评价分页锁
     *
     * @param remark 评价
     * @return 评价锁获取状态枚举
     */
    public RemarkLockEnum pushAndLock(Remark remark) {
        remark.setCreateTime(Sunday.getTime());
        List<String> list = new ArrayList<>();

        //代理键名
        list.add(RedisKeyConstants.REMARK_PROXY_PREFIX + remark.getItemId());

        //评价键名
        list.add(RedisKeyConstants.REMARK_PREFIX + remark.getItemId());

        //锁键名
        list.add(MessageFormat.format(RedisKeyConstants.LOCK_PATTERN, "remark", remark.getItemId()));

        //评分键
        list.add(RedisKeyConstants.REMARK_SCORE_PREFIX + remark.getItemId());

        Long code = redisTemplate.execute(pushAndLockScript, list, JSONObject.fromObject(remark).toString(), remark.getId(), 2, remark.getScore());
        if (code == null)
            return RemarkLockEnum.ERROR;
        if (code == 0)
            return RemarkLockEnum.SUCCESS;
        if (code == 1)
            return RemarkLockEnum.ABORT;
        if (code == 2)
            return RemarkLockEnum.REDO;

        return RemarkLockEnum.LOCK_OK;
    }

    /**
     * 查询评价，并获取评价分页锁
     */
    public Long readAndLock(String itemId) {
        List<String> list = new ArrayList<>();

        //代理键名
        list.add(RedisKeyConstants.REMARK_PROXY_PREFIX + itemId);

        //评价键名
        list.add(RedisKeyConstants.REMARK_PREFIX + itemId);

        //锁键名
        list.add(MessageFormat.format(RedisKeyConstants.LOCK_PATTERN, "remark", itemId));

        return redisTemplate.execute(readAndLockScript, list, 4);
    }

    /**
     * 更新评价缓存，并释放评价分页锁
     *
     * @param itemId 内容id
     * @param id     锁的签名id
     * @param contents   内容列表
     */
    public void swapAndRelease(String itemId, long id, List<Remark> contents) {
        List<String> list = new ArrayList<>();

        //评价键名
        list.add(RedisKeyConstants.REMARK_PREFIX + itemId);

        //代理键名
        list.add(RedisKeyConstants.REMARK_PROXY_PREFIX + itemId);

        //锁键名
        list.add(MessageFormat.format(RedisKeyConstants.LOCK_PATTERN, "remark", itemId));

        //评价内容数组
        JSONArray array = new JSONArray();
        if (contents != null) {
            for (Object o : contents) {
                array.add(JSONObject.fromObject(o));
            }
        }

        redisTemplate.execute(swapScript, list, 3600 * 6, id, array);
    }

    /**
     * 查询评价列表的某个范围
     *
     * @param itemId 内容id
     * @param start  开始索引
     * @param stop   中止索引
     */
    public JSONArray queryRange(String itemId, int start, int stop) {
        String key = RedisKeyConstants.REMARK_PREFIX + itemId;
        return range(start, stop, key);
    }

    private JSONArray range(int start, int stop, String key) {
        List<Object> list = redisTemplate.opsForList().range(key, start, stop);
        JSONArray array = new JSONArray();
        if (list != null) {
            for (Object o : list) {
                array.add(JSONObject.fromObject(o));
            }
        }

        return array;
    }

    /**
     * 读取所有key的值
     *
     * @param keys key的列表
     * @return value的列表
     */
    public List<Object> readAll(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 刷新评价列表
     */
    public void refresh(String itemId, short score) {
        redisTemplate.delete(RedisKeyConstants.REMARK_PROXY_PREFIX + itemId);
        redisTemplate.opsForHash().delete(RedisKeyConstants.REMARK_SCORE_PREFIX + itemId, score);
    }

    /**
     * 读取评分列表
     */
    public Map<Object, Object> readScoreData(String itemId) {
        return redisTemplate.opsForHash().entries(RedisKeyConstants.REMARK_SCORE_PREFIX + itemId);
    }

    /**
     * 保存评分列表
     */
    public void saveScoreData(String itemId, Map<Object, Object> map) {
        redisTemplate.opsForHash().putAll(RedisKeyConstants.REMARK_SCORE_PREFIX + itemId, map);
    }

    public boolean lockScoreData(String itemId) {
        return redisTemplate.opsForValue().setIfAbsent(RedisKeyConstants.REMARK_SCORE_LOCK_PREFIX + itemId, 1, Duration.ofSeconds(10));
    }

    //保存推荐内容并重置过期时间
    public void saveRecommendData(String itemId, /*not null*/ List<Remark> list) {
        String[] argv = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            argv[i] = JSONObject.fromObject(list.get(i)).toString();
        }
        redisTemplate.execute(resetListScript,
                List.of(RedisKeyConstants.REMARK_RECOMMEND_PREFIX + itemId,
                        RedisKeyConstants.REMARK_RECOMMEND_PROXY_PREFIX + itemId), argv);
    }

    //读取推荐内容
    public JSONArray readRecommendRange(String itemId, int start, int stop) {
        String key = RedisKeyConstants.REMARK_RECOMMEND_PREFIX + itemId;
        return range(start, stop, key);
    }

    //是否应该更新推荐
    public boolean shouldUpdateRecommend(String itemId) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(RedisKeyConstants.REMARK_RECOMMEND_PROXY_PREFIX + itemId, 1, Duration.ofSeconds(2));
        return flag == null || !flag;
    }
}
