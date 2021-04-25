package com.sunday.remark.component;

import ai.grakn.redismock.RedisServer;

import java.io.IOException;

public class BaseTest {
    private static RedisServer redisServer = null;

    static {
        try {
            redisServer = RedisServer.newRedisServer();
            redisServer.start();
            System.setProperty("spring.redis.port", Integer.toString(redisServer.getBindPort()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
