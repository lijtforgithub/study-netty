package com.ljt.study.gateway.core;

import com.alibaba.fastjson.JSON;
import com.ljt.study.game.util.RedisUtils;
import com.ljt.study.gateway.GatewayServer;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * @author LiJingTang
 * @date 2022-04-27 14:53
 */
@Slf4j
public final class RedisPubSub {

    private static final String LOGOUT = "logout";

    private RedisPubSub() {}

    public static void pubLogout(String gatewayId, Integer userId) {
        try (Jedis jedis = RedisUtils.getJedis()) {
            LogoutDTO dto = new LogoutDTO(gatewayId, userId);
            jedis.publish(LOGOUT, JSON.toJSONString(dto));
            log.info("发布登出：{}", gatewayId);
        }
    }

    public static void init() {
        Executors.newSingleThreadExecutor().submit(RedisPubSub::subLogout);
    }

    public static void subLogout() {
        Jedis jedis = RedisUtils.getJedis();
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                log.info("收到订阅消息：{} {}", channel, message);
                LogoutDTO dto = JSON.parseObject(message, LogoutDTO.class);
                if (!GatewayServer.getId().equals(dto.getGatewayId())) {
                    return;
                }
                log.info("下线用户：{}", dto.getUserId());
                Channel ch = SessionManage.getChannelByUserId(dto.getUserId());
                if (Objects.nonNull(ch)) {
                    try {
                        ch.disconnect().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    compareAndDel(dto.gatewayId, dto.getUserId());
                }
            }
        }, LOGOUT);
    }

    public static void compareAndDel(String gatewayId, Integer userId) {
        try (Jedis jedis = RedisUtils.getJedis();
             InputStream inputStream = RedisUtils.class.getResourceAsStream("/lua/compareAndDel-gateway.lua")){
            String script = IOUtils.toString(Objects.requireNonNull(inputStream, "脚本为空"));
            jedis.eval(script, 2, userId.toString(), gatewayId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class LogoutDTO {
        private String gatewayId;
        private Integer userId;
    }

}
