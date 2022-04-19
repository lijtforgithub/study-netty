package com.ljt.study.gateway.core;

import com.ljt.study.game.enums.MsgTypeEnum;
import com.ljt.study.game.util.RedisUtils;
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
        int userId = byteBuf.readInt();
        log.info("返回sessionId={} userId={}", sessionId, userId);

        short type = byteBuf.readShort();
        boolean isLoginCmd = MsgTypeEnum.LOGIN == MsgTypeEnum.getEnum(type);

        // 在返回的是否判断用户已经登录原因是 有可能是多种方式的登录 请求参数不一样 但是返回消息可以一样
        if (isLoginCmd) {
            Channel channel = SessionManage.getChannelByUserId(userId);
            if (Objects.nonNull(channel)) {
                SessionManage.sendMsg(ctx, "您已经登录：" + RedisUtils.getUser(userId));
                channel.disconnect().sync();
            } else {
                channel = SessionManage.getChannelBySessionId(sessionId);
                SessionManage.setUserId(channel, userId);
            }
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
