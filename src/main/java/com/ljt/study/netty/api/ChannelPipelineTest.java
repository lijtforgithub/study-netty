package com.ljt.study.netty.api;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author LiJingTang
 * @date 2025-01-16 09:14
 */

public class ChannelPipelineTest {

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(4);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ChannelInHandler("in-A", false))
                                .addLast(new ChannelInHandler("in-B", false))
                                .addLast(new ChannelInHandler("in-C", true));
                        ch.pipeline()
                                .addLast(new ChannelOutHandler("out-A"))
                                .addLast(new ChannelOutHandler("out-B"))
                                .addLast(new ChannelOutHandler("out-C"));
                    }
                });
        bootstrap.bind(8080).syncUninterruptibly();
    }

}

@Slf4j
class ChannelInHandler extends ChannelInboundHandlerAdapter {

    private final String name;
    private final boolean flush;

    public ChannelInHandler(String name, boolean flush) {
        this.name = name;
        this.flush = flush;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("InBoundHandler = {} ctx = {}", name, ctx.hashCode());

        if (flush) {
            ctx.channel().writeAndFlush(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

}

@Slf4j
class ChannelOutHandler extends ChannelOutboundHandlerAdapter {

    private final String name;

    public ChannelOutHandler(String name) {
        this.name = name;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log.info("OutBoundHandler = {} ctx = {}", name, ctx.hashCode());
        super.write(ctx, msg, promise);
    }

}
