package com.ljt.study.game.util;

import com.alibaba.fastjson.JSON;
import com.ljt.study.game.model.User;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2022-04-19 10:33
 */
public final class RedisUtils {

    private RedisUtils() {
    }

    private static final JedisPool POOL = new JedisPool();
    private static final String BASE_INFO = "baseInfo";
    private static final String GATEWAY = "gateway";

    public static Jedis getJedis() {
        return POOL.getResource();
    }

    public static void saveUser(User user) {
        if (Objects.isNull(user)) {
            return;
        }

        try (Jedis jedis = POOL.getResource()) {
            jedis.hset(getUserKey(user.getId()), BASE_INFO, JSON.toJSONString(user));
        }
    }

    public static User getUser(int userId) {
        try (Jedis jedis = POOL.getResource()) {
            String json = jedis.hget(getUserKey(userId), BASE_INFO);
            if (StringUtils.isBlank(json)) {
                return null;
            }
            return JSON.parseObject(json, User.class);
        }
    }

    public static String getGatewayId(int userId) {
        try (Jedis jedis = POOL.getResource()) {
            return jedis.hget(getUserKey(userId), GATEWAY);
        }
    }

    public static boolean setNxGatewayId(int userId, String gatewayId) {
        try (Jedis jedis = POOL.getResource()) {
            return jedis.hsetnx(getUserKey(userId), GATEWAY, gatewayId) != 0;
        }
    }

    public static void setGatewayId(int userId, String gatewayId) {
        try (Jedis jedis = POOL.getResource()) {
            jedis.hset(getUserKey(userId), GATEWAY, gatewayId);
        }
    }

    private static String getUserKey(int userId) {
        return "game:user:" + userId;
    }

}
