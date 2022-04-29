package com.ljt.study.game.util;

import com.alibaba.fastjson.JSON;
import com.ljt.study.game.core.ChannelUserManage;
import com.ljt.study.gateway.GatewayServer;
import com.ljt.study.gateway.core.SessionManage;
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
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * @author LiJingTang
 * @date 2022-04-27 14:53
 */
@Slf4j
public final class RedisPubSub {

    private static final String LOGOUT = "logout";
    private static final String OFFER_LINE = "offer_line";

    private RedisPubSub() {}

    public static void pubOfferLine(Integer userId) {
        try (Jedis jedis = RedisUtils.getJedis()) {
            jedis.publish(OFFER_LINE, userId.toString());
            log.info("发布下线：{}", userId);
        }
    }

    public static void subOfferLine() {
        Executors.newSingleThreadExecutor().submit(() -> RedisUtils.getJedis().subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                log.info("收到订阅消息：{} {}", channel, message);
                Integer userId = Integer.valueOf(message);
                ChannelUserManage.removeUser(userId);
            }
        }, OFFER_LINE));
    }

    public static void pubLogout(String gatewayId, Integer userId) {
        try (Jedis jedis = RedisUtils.getJedis()) {
            LogoutDTO dto = new LogoutDTO(gatewayId, userId);
            jedis.publish(LOGOUT, JSON.toJSONString(dto));
            log.info("发布登出：{}", gatewayId);
        }
    }

    public static void subLogout() {
        Executors.newSingleThreadExecutor().submit(() -> RedisUtils.getJedis().subscribe(new JedisPubSub() {
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
        }, LOGOUT));
    }

    public static void compareAndDel(String gatewayId, Integer userId) {
        try (Jedis jedis = RedisUtils.getJedis();
             InputStream inputStream = RedisUtils.class.getResourceAsStream("/lua/compareAndDel-gateway.lua")){
            String script = IOUtils.toString(Objects.requireNonNull(inputStream, "脚本为空"), Charset.defaultCharset());
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
