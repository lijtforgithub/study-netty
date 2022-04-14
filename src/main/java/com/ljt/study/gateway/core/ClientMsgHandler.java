package com.ljt.study.gateway.core;

import com.ljt.study.game.enums.ServiceTypeEnum;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Random;

/**
 * @author LiJingTang
 * @date 2022-04-10 22:13
 */
@Slf4j
public class ClientMsgHandler extends ChannelHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SessionManage.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SessionManage.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        int i = new Random().nextInt(3) - 1;
        NettyClient client = ServiceDiscovery.getClientByType(ServiceTypeEnum.values()[i]);
        if (Objects.nonNull(client) && msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
            BinaryWebSocketFrame newFrame = new BinaryWebSocketFrame(frame.content().copy());
            client.sendMsg(newFrame);
        } else {
            super.channelRead(ctx, msg);
        }
    }

}
