package com.ljt.study.gateway.core;

import com.ljt.study.game.enums.MsgTypeEnum;
import com.ljt.study.game.enums.ServiceTypeEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 这里不能继承SimpleChannelInboundHandler 因为消息没重新封装 要一直传递下去
 *
 * @author LiJingTang
 * @date 2022-04-28 15:44
 */
@Slf4j
public class RouteHandler extends ChannelHandlerAdapter {

    private static final String CLIENT = "netty_client";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        NettyClient client = getNettyClient(ctx.channel());
        if (Objects.isNull(client)) {
            log.warn("NettyClient为空");
            SessionManage.sendMsg(ctx, "服务器异常...");
            return;
        }

        client.sendMsg(msg);
    }

    private NettyClient getNettyClient(Channel channel) {
        boolean isLoginCmd = MsgTypeEnum.LOGIN == MsgTypeEnum.getEnum(SessionManage.getMsgType(channel));

        if (isLoginCmd) {
            return ServiceDiscovery.getClientByType(ServiceTypeEnum.LOGIN);
        } else {
            Object instanceId = channel.attr(AttributeKey.valueOf(CLIENT)).get();
            if (Objects.nonNull(instanceId)) {
                return ServiceDiscovery.getClientById(instanceId.toString());
            }

            NettyClient client = ServiceDiscovery.getClientByType(ServiceTypeEnum.GAME);
            if (Objects.nonNull(client)) {
                channel.attr(AttributeKey.valueOf(CLIENT)).set(client.getServiceId());
            }
            return client;
        }
    }

}
