package com.ljt.study.gateway.core;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
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
    private static final String KEY_USER_ID = "user_id";
    private static final AtomicInteger SESSION_ID = new AtomicInteger(1);
    private static final ConcurrentHashMap<Integer, Channel> MAP = new ConcurrentHashMap<>();
    private static final ChannelGroup GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private SessionManage() {}

    public static Integer addChannel(Channel channel) {
        GROUP.add(channel);
        Integer sessionId = SESSION_ID.getAndIncrement();
        channel.attr(AttributeKey.valueOf(KEY_SESSION_ID)).setIfAbsent(sessionId);
        MAP.put(sessionId, channel);
        log.info("添加channel {} = {}", channel.id(), sessionId);
        return sessionId;
    }

    public static void remove(Channel channel) {
        GROUP.remove(channel);
        Integer sessionId = getSessionId(channel);
        if (Objects.nonNull(sessionId) && MAP.containsKey(sessionId)) {
            MAP.remove(sessionId);
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

    public static void setUserId(Channel channel, Integer userId) {
        if (Objects.isNull(userId)) {
            return;
        }
        channel.attr(AttributeKey.valueOf(KEY_USER_ID)).setIfAbsent(userId);
    }

    public static Integer getUserId(Channel channel) {
        Object value = channel.attr(AttributeKey.valueOf(KEY_USER_ID)).get();
        if (Objects.isNull(value)) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    public static Channel getChannel(Integer sessionId) {
        return MAP.get(sessionId);
    }

    public static void broadcast(Object msg) {
        GROUP.writeAndFlush(msg);
    }

}
