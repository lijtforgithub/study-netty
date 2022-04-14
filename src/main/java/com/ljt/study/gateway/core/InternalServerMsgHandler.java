package com.ljt.study.gateway.core;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * @author LiJingTang
 * @date 2022-04-11 22:13
 */
public class InternalServerMsgHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BinaryWebSocketFrame)) {
            super.channelRead(ctx, msg);
            return;
        }

        SessionManage.broadcast(msg);

    }

}
