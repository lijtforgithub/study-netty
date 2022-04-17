package com.ljt.study.game.core;

import com.ljt.study.game.msg.BaseMsg;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author LiJingTang
 * @date 2022-04-10 11:22
 */
public final class HandlerContext {

    private final ChannelHandlerContext channelContext;
    private final Integer sessionId;

    public HandlerContext(ChannelHandlerContext channelContext, Integer sessionId) {
        this.channelContext = channelContext;
        this.sessionId = sessionId;
    }

    public ChannelFuture writeAndFlush(BaseMsg msg) {
        MsgDTO dto = new MsgDTO();
        dto.setMsg(msg);
        dto.setSessionId(sessionId);

        return channelContext.writeAndFlush(dto);
    }

}
