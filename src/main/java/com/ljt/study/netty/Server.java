package com.ljt.study.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.ljt.study.Constant.LOCAL_HOST;
import static com.ljt.study.Constant.DEF_PORT;

/**
 * @author LiJingTang
 * @date 2020-05-08 13:54
 */
@Slf4j
@AllArgsConstructor
public class Server {

    private int port;
    private String host;
    private ChannelHandler channelHandler;

    public Server(ChannelHandler channelHandler) {
        this.port = DEF_PORT;
        this.host = LOCAL_HOST;
        this.channelHandler = channelHandler;
    }

    public void start() {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(channelHandler);
            // 绑定端口 同步等待成功
            ChannelFuture future = bootstrap.bind(host, port).sync();
            log.info("服务端启动成功 {}:{}", host, port);
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

}
