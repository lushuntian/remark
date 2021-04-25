package com.sunday.remark.config.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 评价缓存容量配置
 */
@Configuration
public class RemarkCacheConfig {
    @Bean("remarkListCache")
    //评价列表缓存
    public Cache<Object, Object> remarkListCache(){
        //一个元素大概5K
        return Caffeine.newBuilder()
                .initialCapacity(1000)
                .maximumSize(20000)
                .expireAfterWrite(Duration.ofSeconds(10))
                .softValues()
                .build(key -> null);
    }

    @Bean("scoreListCache")
    //评价列表缓存
    public Cache<Object, Object> scoreListCache(){
        //一个元素大概100 byte
        return Caffeine.newBuilder()
                .initialCapacity(1000)
                .maximumSize(200000)
                .expireAfterWrite(Duration.ofSeconds(10))
                .softValues()
                .build(key -> null);
    }

    @Bean("recommendListCache")
    //评价推荐缓存
    public Cache<Object, Object> recommendListCache(){
        //一个元素大概5K
        return Caffeine.newBuilder()
                .initialCapacity(1000)
                .maximumSize(20000)
                .expireAfterWrite(Duration.ofSeconds(10))
                .softValues()
                .build(key -> null);
    }

    @Bean
    public CacheManager caffeineCacheManager(Cache<Object, Object> remarkListCache, Cache<Object, Object> scoreListCache, Cache<Object, Object> recommendListCache) {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        List<CaffeineCache> caffeineCaches = new ArrayList<>();

        caffeineCaches.add(new CaffeineCache("remark", remarkListCache));
        caffeineCaches.add(new CaffeineCache("score", scoreListCache));
        caffeineCaches.add(new CaffeineCache("recommend", recommendListCache));

        simpleCacheManager.setCaches(caffeineCaches);
        return simpleCacheManager;
    }
}








