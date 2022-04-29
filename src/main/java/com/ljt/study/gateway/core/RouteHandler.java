package com.ljt.study.gateway.core;

import com.ljt.study.game.enums.MsgTypeEnum;
import com.ljt.study.game.enums.ServiceTypeEnum;
import com.ljt.study.game.processor.AsyncProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import java.util.Objects;

/**
 * 这里不能继承SimpleChannelInboundHandler 因为消息没重新封装 要一直传递下去
 *
 * @author LiJingTang
 * @date 2022-04-28 15:44
 */
@Slf4j
public class RouteHandler extends ChannelInboundHandlerAdapter {

    private static final String CLIENT = "netty_client";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // IO放到异步线程处理 不影响netty线程池
        AsyncProcessor.process(RandomUtils.nextInt(), () -> getNettyClient(ctx.channel()),
                client -> ctx.executor().submit(() -> {
                    if (Objects.isNull(client)) {
                        log.warn("NettyClient为空");
                        SessionManage.sendMsg(ctx, "服务器异常...");
                        return;
                    }
                    client.sendMsg(msg);
                }));

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
