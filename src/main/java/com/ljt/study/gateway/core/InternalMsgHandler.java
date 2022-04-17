package com.ljt.study.gateway.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2022-04-11 22:13
 */
@Slf4j
public class InternalMsgHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BinaryWebSocketFrame)) {
            super.channelRead(ctx, msg);
            return;
        }

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = frame.content();
        int sessionId = byteBuf.readInt();
        log.info("返回sessionId={}", sessionId);

        if (-1 == sessionId) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            SessionManage.broadcast(bytes);
        } else {
            ByteBuf newByteBuf = ctx.alloc().buffer();
            newByteBuf.writeBytes(byteBuf);
            BinaryWebSocketFrame newFrame = new BinaryWebSocketFrame(newByteBuf);
            Channel channel = SessionManage.getChannel(sessionId);
            if (Objects.nonNull(channel)) {
                channel.writeAndFlush(newFrame);
            }
        }
    }

}
