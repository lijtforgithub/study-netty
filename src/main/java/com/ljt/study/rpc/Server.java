package com.ljt.study.rpc;

import com.ljt.study.rpc.handler.CustomProtocolDecode;
import com.ljt.study.rpc.handler.CustomRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.ljt.study.rpc.RpcUtils.ADDRESS;

/**
 * @author LiJingTang
 * @date 2021-03-06 22:26
 */
@Slf4j
public class Server {

    public void start() {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(10);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new CustomProtocolDecode())
                                    .addLast(new CustomRequestHandler());
                        }
                    });
            // 绑定端口 同步等待成功
            ChannelFuture future = bootstrap.bind(ADDRESS).sync();
            log.info("服务端启动成功 {}", ADDRESS);
            // 等待服务端口监听端口关闭
            future.channel().closeFuture().sync();
            log.info("服务端关闭");
        } catch (InterruptedException e) {
            log.error(StringUtils.EMPTY, e);
        }
    }

}
