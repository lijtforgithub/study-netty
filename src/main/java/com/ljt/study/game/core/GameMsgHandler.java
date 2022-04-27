package com.ljt.study.game.core;

import com.ljt.study.game.handler.MsgHandler;
import com.ljt.study.game.model.MsgDTO;
import com.ljt.study.game.processor.MainProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2022-04-09 17:47
 */
@Slf4j
public class GameMsgHandler extends SimpleChannelInboundHandler<MsgDTO> {

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
    protected void messageReceived(ChannelHandlerContext ctx, MsgDTO dto) {
        MsgHandler<?> handler = MsgHandlerFactory.getHandler(dto.getMsg().getClass());
        if (Objects.isNull(handler)) {
            log.warn("无法处理消息：{}", dto.getMsg().getClass().getName());
            return;
        }

        MainProcessor.process(ctx, dto, handler);
    }

}
