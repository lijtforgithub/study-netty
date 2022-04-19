package com.ljt.study.gateway.core;

import com.ljt.study.game.enums.MsgTypeEnum;
import com.ljt.study.game.enums.ServiceTypeEnum;
import com.ljt.study.game.util.RedisUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

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
        Integer sessionId = SessionManage.getSessionId(ctx.channel());
        if (Objects.isNull(sessionId) || (!(msg instanceof BinaryWebSocketFrame))) {
            super.channelRead(ctx, msg);
            log.warn("sessionId为空");
            return;
        }

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = frame.content();
        short type = byteBuf.readShort();
        boolean isLoginCmd = MsgTypeEnum.LOGIN == MsgTypeEnum.getEnum(type);

        ServiceTypeEnum typeEnum = isLoginCmd ? ServiceTypeEnum.LOGIN : ServiceTypeEnum.GAME;
        Integer userId = SessionManage.getUserId(ctx.channel());

        if (isLoginCmd) {
            if (Objects.nonNull(userId)) {
                writeMsg(ctx, "您已经登录：" + RedisUtils.getUser(userId));
                return;
            }
        } else {
            if (Objects.isNull(userId)) {
                writeMsg(ctx, "请先登录...");
                return;
            }
        }

        NettyClient client = ServiceDiscovery.getClientByType(typeEnum);
        if (Objects.isNull(client)) {
            log.warn("NettyClient为空");
            return;
        }

        ByteBuf newByteBuf = ctx.alloc().buffer();
        newByteBuf.writeInt(sessionId);
        newByteBuf.writeInt(Optional.ofNullable(userId).orElse(0));
        newByteBuf.writeShort(type);
        newByteBuf.writeBytes(byteBuf);

        // frame.content().copy()
        BinaryWebSocketFrame newFrame = new BinaryWebSocketFrame(newByteBuf);
        client.sendMsg(newFrame);
    }

    private void writeMsg(ChannelHandlerContext ctx, String msg) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeShort(0);
        byteBuf.writeBytes(msg.getBytes(StandardCharsets.UTF_8));
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(byteBuf);
        ctx.writeAndFlush(frame);
    }

}
