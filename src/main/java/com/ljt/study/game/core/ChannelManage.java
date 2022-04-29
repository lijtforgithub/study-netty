package com.ljt.study.game.core;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.ljt.study.game.model.MsgDTO;
import com.ljt.study.game.msg.BaseMsg;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.ljt.study.gateway.core.SessionManage.KEY_GATEWAY_ID;

/**
 * @author LiJingTang
 * @date 2022-04-09 22:04
 */
@Slf4j
public final class ChannelManage {

    private ChannelManage() {
    }

    private static final Map<Integer, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final Set<SessionDTO> SESSION = new ConcurrentHashSet<>();

    public static void broadcastAll(BaseMsg msg) {
        MsgDTO dto = new MsgDTO();
        dto.setMsg(msg);
        dto.setSessionId(-1);
        dto.setUserId(-1);

        CHANNEL_MAP.values().forEach(channel -> channel.writeAndFlush(dto));
    }

    public static void broadcastCurrentServer(BaseMsg msg) {
        MsgDTO dto = new MsgDTO();
        dto.setMsg(msg);

        SESSION.forEach(sessionDTO -> {
            dto.setSessionId(sessionDTO.getSessionId());
            dto.setUserId(sessionDTO.getUserId());
            Channel channel = CHANNEL_MAP.get(sessionDTO.getGatewayId());
            if (Objects.nonNull(channel)) {
                channel.writeAndFlush(dto);
            }
        });
    }

    public static Integer addChannel(Channel channel) {
        Integer gatewayId = channel.attr(AttributeKey.<Integer>valueOf(KEY_GATEWAY_ID)).get();
        if (Objects.isNull(gatewayId)) {
            return null;
        }
        CHANNEL_MAP.putIfAbsent(gatewayId, channel);
        log.info("添加channel {}", gatewayId);
        return gatewayId;
    }

    public static void removeChannel(Channel channel) {
        Integer gatewayId = channel.attr(AttributeKey.<Integer>valueOf(KEY_GATEWAY_ID)).get();
        CHANNEL_MAP.remove(gatewayId);
        log.info("删除channel {}", gatewayId);
    }

    public static void addUser(Channel channel, Integer sessionId, Integer userId) {
        Integer gatewayId = addChannel(channel);
        if (ObjectUtils.anyNull(gatewayId, sessionId, userId) || sessionId <= 0 || userId <= 0) {
            return;
        }

        SESSION.add(new SessionDTO(gatewayId, sessionId, userId));
    }

    public static void removeUser(Integer userId) {
        SESSION.removeIf(dto -> dto.getUserId().equals(userId));
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SessionDTO {
        private Integer gatewayId;
        private Integer sessionId;
        private Integer userId;
    }

}
