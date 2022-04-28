package com.ljt.study.gateway.core;

import com.ljt.study.gateway.GatewayServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

/**
 * @author LiJingTang
 * @date 2022-04-10 22:13
 */
@Slf4j
public class ClientMsgHandler extends ChannelHandlerAdapter {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        if (Objects.isNull(pipeline.get(ValidateUserHandler.class))) {
            pipeline.addBefore(ctx.name(), ValidateUserHandler.class.getName(), new ValidateUserHandler());
        }
        if (Objects.isNull(pipeline.get(RouteHandler.class))) {
            pipeline.addAfter(ctx.name(), RouteHandler.class.getName(), new RouteHandler());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SessionManage.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        Integer userId = SessionManage.getUserId(ctx.channel());
        if (Objects.nonNull(userId)) {
            RedisPubSub.compareAndDel(GatewayServer.getId(), userId);
            log.info("用户下线：{}", userId);
        }

        SessionManage.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Integer sessionId = SessionManage.getSessionId(ctx.channel());
        if (Objects.isNull(sessionId) || !(msg instanceof BinaryWebSocketFrame)) {
            log.warn("sessionId为空 {}", sessionId);
            return;
        }

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = frame.content();
        short type = byteBuf.readShort();
        Integer userId = SessionManage.getUserId(ctx.channel());

        ByteBuf newByteBuf = ctx.alloc().buffer();
        newByteBuf.writeInt(sessionId);
        newByteBuf.writeInt(Optional.ofNullable(userId).orElse(0));
        newByteBuf.writeShort(type);
        newByteBuf.writeBytes(byteBuf);

        // frame.content().copy()
        BinaryWebSocketFrame newFrame = new BinaryWebSocketFrame(newByteBuf);
        ctx.fireChannelRead(newFrame);
    }

}
