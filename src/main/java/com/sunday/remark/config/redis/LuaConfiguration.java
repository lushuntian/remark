package com.sunday.remark.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * Lua脚本
 */
@Configuration
public class LuaConfiguration {
    /**
     * [点赞]脚本  lua_set_and_incr
     */
    @Bean
    public DefaultRedisScript<Integer> voteScript() {
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/lua_set_and_incr.lua")));
        redisScript.setResultType(Integer.class);
        return redisScript;
    }

    /**
     * [取消点赞]脚本  lua_del_and_decr
     */
    @Bean
    public DefaultRedisScript<Integer> noVoteScript() {
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/lua_del_and_decr.lua")));
        redisScript.setResultType(Integer.class);
        return redisScript;
    }

    /**
     * [插入评价，获取评价分页锁]脚本  lua_add_remark.lua
     */
    @Bean
    public DefaultRedisScript<Long> pushAndLockScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/lua_add_remark.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * [读取评价锁]脚本  lua_read_remark.lua
     */
    @Bean
    public DefaultRedisScript<Long> readAndLockScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/lua_read_remark.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * [释放评价分页锁]脚本  lua_swap_remark.lua
     */
    @Bean
    public DefaultRedisScript<Object> swapAndReleaseScript() {
        DefaultRedisScript<Object> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/lua_swap_remark.lua")));
        redisScript.setResultType(Object.class);
        return redisScript;
    }

    /**
     * [优先级锁]脚本  lua_priority_lock.lua
     */
    @Bean
    public DefaultRedisScript<Boolean> priorityLockScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/lua_priority_lock.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }

    /**
     * [重建列表]脚本  lua_reset_list.lua
     */
    @Bean
    public DefaultRedisScript<Boolean> resetListScript() {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("script/lua_reset_list.lua")));
        redisScript.setResultType(Boolean.class);
        return redisScript;
    }

}
