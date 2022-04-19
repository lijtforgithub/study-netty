package com.ljt.study.gateway.core;

import com.alibaba.fastjson.JSON;
import com.ljt.study.game.enums.MsgTypeEnum;
import com.ljt.study.game.msg.LoginMsg;
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

        short type = byteBuf.readShort();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        boolean isLoginCmd = MsgTypeEnum.LOGIN == MsgTypeEnum.getEnum(type);
        if (isLoginCmd) {
            Channel channel = SessionManage.getChannel(sessionId);
            LoginMsg loginMsg = JSON.parseObject(new String(bytes), LoginMsg.class);
            SessionManage.setUserId(channel, loginMsg.getUserId());
        }

        ByteBuf newByteBuf = ctx.alloc().buffer();
        newByteBuf.writeShort(type);
        newByteBuf.writeBytes(bytes);
        BinaryWebSocketFrame newFrame = new BinaryWebSocketFrame(newByteBuf);

        if (-1 == sessionId) {
            SessionManage.broadcast(newFrame);
        } else {
            Channel channel = SessionManage.getChannel(sessionId);
            if (Objects.nonNull(channel) && channel.isWritable()) {
                channel.writeAndFlush(newFrame);
            }
        }
    }

}
