package com.ljt.study.gateway;

import com.ljt.study.gateway.core.ClientMsgHandler;
import com.ljt.study.gateway.core.ServiceDiscovery;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author LiJingTang
 * @date 2022-04-10 21:29
 */
@Slf4j
class GatewayServer {

    public static void main(String[] args) {
        ServiceDiscovery.findService();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 500)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new HttpServerCodec(), // 添加 Http 编解码器
                                    new HttpObjectAggregator(65535), // 内容不能太长
                                    new WebSocketServerProtocolHandler("/websocket"), // WebSocket 协议
                                    new ClientMsgHandler()  // 最后在这里处理游戏消息
                            );
                        }
                    });

            int port = 12345;
            ChannelFuture future = bootstrap.bind(port).sync();

            if (future.isSuccess()) {
                log.info("服务启动成功：{} ", port);
            }

            future.channel().closeFuture().sync();
            log.info("服务关闭");
        } catch (Exception e) {
            log.error("服务启动失败", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

}
