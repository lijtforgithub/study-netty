package com.ljt.study.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.ljt.study.Constant.*;

/**
 * @author LiJingTang
 * @date 2020-04-30 17:16
 */
@Slf4j
@AllArgsConstructor
public class TimeServer {

    public TimeServer() {
        this(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new TimeServerHandler());
            }
        });
    }

    public TimeServer(ChannelHandler channelHandler) {
        this.port = PORT;
        this.channelHandler = channelHandler;
    }

    private int port;
    private ChannelHandler channelHandler;

    public void start() {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(channelHandler);
            // 绑定端口 同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();
            log.info("服务端启动成功");
            // 等待服务端口监听端口关闭
            future.channel().closeFuture().sync();
            log.info("服务端关闭");
        } catch (InterruptedException e) {
            log.error(StringUtils.EMPTY, e);
        } finally {
            // 优雅退出 释放线程池资源
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Slf4j
    private static class TimeServerHandler extends ChannelHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] req = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(req);
            String body = new String(req);
            log.info("服务端接收到数据：{}", body);

            String currentTime = TIMER_ORDER.equalsIgnoreCase(body) ? LocalDateTime.now().toString() : TIMER_BAD;
            ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes(StandardCharsets.UTF_8));
            ctx.write(resp);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }

}
