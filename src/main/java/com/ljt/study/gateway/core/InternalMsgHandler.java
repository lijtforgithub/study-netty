package com.ljt.study.gateway.core;

import com.ljt.study.game.enums.MsgTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2022-04-11 22:13
 */
@Slf4j
public class InternalMsgHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        if (Objects.isNull(pipeline.get(CheckLoginHandler.class))) {
            pipeline.addBefore(ctx.name(), CheckLoginHandler.class.getName(), new CheckLoginHandler());
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
        ByteBuf byteBuf = frame.content();
        int sessionId = byteBuf.readInt();
        int userId = byteBuf.readInt();
        short type = byteBuf.readShort();
        boolean isLoginCmd = MsgTypeEnum.LOGIN == MsgTypeEnum.getEnum(type);

        if (isLoginCmd) {
            Channel channel = SessionManage.getChannelBySessionId(sessionId);
            SessionManage.setUserId(channel, userId);
        }

        ByteBuf newByteBuf = ctx.alloc().buffer();
        newByteBuf.writeShort(type);
        newByteBuf.writeBytes(byteBuf);
        BinaryWebSocketFrame newFrame = new BinaryWebSocketFrame(newByteBuf);

        if (-1 == sessionId) {
            SessionManage.broadcast(newFrame);
        } else {
            Channel channel = SessionManage.getChannelBySessionId(sessionId);
            if (Objects.nonNull(channel) && channel.isWritable()) {
                channel.writeAndFlush(newFrame);
            }
        }
    }

}
