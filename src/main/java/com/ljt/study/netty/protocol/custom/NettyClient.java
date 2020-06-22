package com.ljt.study.netty.protocol.custom;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ljt.study.Constant.LOCAL_HOST;
import static com.ljt.study.Constant.PORT;

/**
 * @author LiJingTang
 * @date 2020-05-11 14:03
 */
@Slf4j
public class NettyClient {

    public static void main(String[] args) {
        new NettyClient().start();
    }

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private EventLoopGroup group = new NioEventLoopGroup();

    private void start() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(new NettyMessageDecoder(1024 * 1024, 4, 4, -8, 0))
                                    .addLast(new NettyMessageEncoder())
                                    .addLast(new ReadTimeoutHandler(50))
                                    .addLast(new LoginAuthReqHandler())
                                    .addLast(new HeartBeatReqHandler());
                        }
                    });
            // 发起异步连接操作
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(LOCAL_HOST, PORT)
                    // 指定客户端端口
//                    , new InetSocketAddress(LOCAL_HOST, 8888)
            ).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error(StringUtils.EMPTY, e);
        } finally {
            executor.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    // 发起重连操作
                    start();
                } catch (InterruptedException e) {
                    log.error(StringUtils.EMPTY, e);
                }
            });
        }
    }

}
