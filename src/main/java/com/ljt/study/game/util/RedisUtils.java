package com.ljt.study.game.util;

import com.alibaba.fastjson.JSON;
import com.ljt.study.game.model.User;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Objects;

import static com.ljt.study.Constant.LOCAL_HOST;

/**
 * @author LiJingTang
 * @date 2022-04-19 10:33
 */
public final class RedisUtils {

    private RedisUtils() {}

    private static final JedisPool POOL;
    private static final String BASE_INFO = "baseInfo";

    static {
        JedisPoolConfig conf = new JedisPoolConfig();
        // 最大连接数
        conf.setMaxTotal(100);
        // 最大空闲数
        conf.setMaxIdle(50);
        // 最大等待毫秒数
        conf.setMaxWaitMillis(20000);
        // 创建连接池
        POOL = new JedisPool(conf, LOCAL_HOST);
    }

    public static void saveUser(User user) {
        if (Objects.isNull(user)) {
            return;
        }
        Jedis jedis = POOL.getResource();
        jedis.hset(getUserKey(user.getId()), BASE_INFO, JSON.toJSONString(user));
    }

    public static User getUser(int userId) {
        Jedis jedis = POOL.getResource();
        String json = jedis.hget(getUserKey(userId), BASE_INFO);
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSON.parseObject(json, User.class);
    }

    private static String getUserKey(int userId) {
        return "game:user:" + userId;
    }

}
