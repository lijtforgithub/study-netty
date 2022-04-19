package com.ljt.study.game.core;

import com.ljt.study.game.model.MsgDTO;
import com.ljt.study.game.processor.MainProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author LiJingTang
 * @date 2022-04-09 17:47
 */
@Slf4j
public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ChannelManage.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ChannelManage.removeChannel(ctx.channel());
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof MsgDTO)) {
            log.warn("未知消息：{}", msg);
            return;
        }

        MsgDTO dto = (MsgDTO) msg;

        MainProcessor.process(ctx, dto);
    }

}
