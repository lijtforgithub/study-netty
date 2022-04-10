package com.ljt.study.game.core;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author LiJingTang
 * @date 2022-04-10 11:22
 */
public final class HandlerContext {

    private final ChannelHandlerContext channelContext;

    public HandlerContext(ChannelHandlerContext channelContext) {
        this.channelContext = channelContext;
    }

    public ChannelFuture writeAndFlush(Object msg) {
        return channelContext.writeAndFlush(msg);
    }

}
