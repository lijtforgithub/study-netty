package com.ljt.study.game.core;

import com.ljt.study.game.handler.MsgHandler;
import com.ljt.study.game.msg.BaseMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2022-04-09 17:47
 */
@Slf4j
public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Broadcaster.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Broadcaster.removeChannel(ctx.channel());
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof BaseMsg)) {
            log.warn("未知消息：{}", msg);
            return;
        }

        MsgHandler<?> handler = MsgHandlerFactory.getHandler(msg.getClass());
        if (Objects.isNull(handler)) {
            log.warn("无法处理消息：{}", msg.getClass().getName());
            return;
        }

        MainProcessor.process(handler, ctx, (BaseMsg) msg);
    }

}
