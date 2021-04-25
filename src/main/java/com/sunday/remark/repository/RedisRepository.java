package com.sunday.remark.repository;

import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * redis仓库。
 * 也可以视为操作redis的工具类
 */
@Repository
public class RedisRepository {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 以指定到期时间设置字符串值
     */
    public void set(String key, String value, long second){
        redisTemplate.opsForValue().set(key, value, second, TimeUnit.SECONDS);
    }

    /**
     * 读取字符串
     * @return 如果不存在或到期，返回null
     */
    public Object read(String key){
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除key
     */
    public void delete(String key){
        redisTemplate.delete(key);
    }

    /**
     * 以指定到期时间设置JSONArray序列化的字符串值
     */
    public void set(String key, JSONArray values, long second){
        set(key, values.toString(), second);
    }

    /**
     * 查询键是否存在
     */
    public boolean exist(String key){
        return redisTemplate.hasKey(key);
    }

    /**
     * 读取字符串的JSONArray对象
     * @return 如果不存在或到期，返回null
     */
    public JSONArray readJsonArray(String key){
        String value = (String) read(key);
        if (value != null)
            return JSONArray.fromObject(value);

        return null;
    }

    /**
     * 执行脚本
     */
    public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }
}
