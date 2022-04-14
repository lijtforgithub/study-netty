package com.ljt.study.gateway.core;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LiJingTang
 * @date 2022-04-10 22:45
 */
@Slf4j
public final class SessionManage {

    private static final String KEY_SESSION_ID = "session_id";
    private static final AtomicInteger SESSION_ID = new AtomicInteger(1);
    private static final ConcurrentHashMap<Integer, Channel> GROUP = new ConcurrentHashMap<>();

    private SessionManage() {}

    public static Integer addChannel(Channel channel) {
        Integer sessionId = SESSION_ID.getAndIncrement();
        channel.attr(AttributeKey.valueOf(KEY_SESSION_ID)).setIfAbsent(sessionId);
        GROUP.put(sessionId, channel);
        log.info("添加channel {} = {}", channel.id(), sessionId);
        return sessionId;
    }

    public static void remove(Channel channel) {
        Integer sessionId = getSessionId(channel);
        if (Objects.nonNull(sessionId) && GROUP.containsKey(sessionId)) {
            GROUP.remove(sessionId);
            log.info("移除channel {} = {}", channel.id(), sessionId);
        }
    }

    public static Integer getSessionId(Channel channel) {
        Object value = channel.attr(AttributeKey.valueOf(KEY_SESSION_ID)).get();
        if (Objects.isNull(value)) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    public static Channel getChannel(Integer sessionId) {
        return GROUP.get(sessionId);
    }

    public static void broadcast(Object msg) {
        GROUP.forEach((k, v) -> {
            if (v.isOpen()) {
                v.writeAndFlush(msg);
            }
        });
    }

}
