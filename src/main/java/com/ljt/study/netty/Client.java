package com.ljt.study.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.ljt.study.Constant.LOCAL_HOST;
import static com.ljt.study.Constant.PORT;

/**
 * @author LiJingTang
 * @date 2020-05-08 13:56
 */
@Slf4j
@AllArgsConstructor
public class Client {

    public Client(ChannelHandler channelHandler) {
        this.port = PORT;
        this.host = LOCAL_HOST;
        this.channelHandler = channelHandler;
    }

    private int port;
    private String host;
    private ChannelHandler channelHandler;

    public void start() {
        // 配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(channelHandler);

            // 发起异步连接操作
            ChannelFuture future = bootstrap.connect(host, port).sync();
            log.info("客户端启动成功");
            // 等待客户端链路关闭
            future.channel().closeFuture().sync();
            log.info("客户端关闭");
        } catch (InterruptedException e) {
            log.error(StringUtils.EMPTY, e);
        } finally {
            group.shutdownGracefully();
        }
    }

}
