package com.sunday.remark.util;

import com.sunday.remark.repository.RedisRepository;
import net.sf.json.JSONArray;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class RedisRepositoryAdapter extends RedisRepository {
    private RedisRepository redisRepository;

    private RedisTemplate<String, Object> redisTemplate;

    public RedisRepositoryAdapter(RedisRepository redisRepository, RedisTemplate<String, Object> redisTemplate) {
        this.redisRepository = redisRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 以指定到期时间设置字符串值
     *
     * @param key
     * @param value
     * @param second
     */
    @Override
    public void set(String key, String value, long second) {
        redisRepository.set(key, value, second);
    }

    /**
     * 读取字符串
     *
     * @param key
     * @return 如果不存在或到期，返回null
     */
    @Override
    public Object read(String key) {
        return redisRepository.read(key);
    }



    /**
     * 删除key
     *
     * @param key
     */
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 以指定到期时间设置JSONArray序列化的字符串值
     *
     * @param key
     * @param values
     * @param second
     */
    @Override
    public void set(String key, JSONArray values, long second) {
        redisRepository.set(key, values, second);
    }

    /**
     * 读取字符串的JSONArray对象
     *
     * @param key
     * @return 如果不存在或到期，返回null
     */
    @Override
    public JSONArray readJsonArray(String key) {
        return redisRepository.readJsonArray(key);
    }



}
