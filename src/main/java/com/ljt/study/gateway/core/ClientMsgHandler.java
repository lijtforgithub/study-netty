package com.ljt.study.gateway.core;

import com.ljt.study.game.enums.MsgTypeEnum;
import com.ljt.study.game.enums.ServiceTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

/**
 * @author LiJingTang
 * @date 2022-04-10 22:13
 */
@Slf4j
public class ClientMsgHandler extends SimpleChannelInboundHandler<Object> {

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
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
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
        Integer userId = SessionManage.getUserId(ctx.channel());

        if (isLoginCmd) {
            if (Objects.nonNull(userId)) {
                SessionManage.sendMsg(ctx, "请勿重复登录");
                return;
            }
        } else {
            if (Objects.isNull(userId)) {
                SessionManage.sendMsg(ctx, "请先登录...");
                return;
            }
        }

        ServiceTypeEnum typeEnum = isLoginCmd ? ServiceTypeEnum.LOGIN : ServiceTypeEnum.GAME;
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

}
