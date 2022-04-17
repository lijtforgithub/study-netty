package com.ljt.study.gateway.core;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author LiJingTang
 * @date 2022-04-10 21:35
 */
@Slf4j
public final class NettyClient {

    private final NioEventLoopGroup WORKER_GROUP = new NioEventLoopGroup(2, r -> {
        Thread t = new Thread(r);
        t.setName("Netty-Client");
        return t;
    });

    private final String serviceId;
    private Consumer<Future<?>> closeCallback;
    private Channel channel;

    public String getServiceId() {
        return serviceId;
    }

    public NettyClient(String serviceId, Consumer<Future<?>> closeCallback) {
        this.serviceId = serviceId;
        this.closeCallback = closeCallback;
    }

    public void connect(String host, int port) throws Exception {
        String uri = String.format("ws://%s:%d", host, port);
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        WebSocketClientHandshaker handshake = WebSocketClientHandshakerFactory.newHandshaker(
                new URI(uri), WebSocketVersion.V13, null, true, httpHeaders);

        Bootstrap bootstrap = new Bootstrap()
                .group(WORKER_GROUP)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new HttpClientCodec(), // Http 客户端编解码器
                                new HttpObjectAggregator(65535),
                                new WebSocketClientProtocolHandler(handshake),
                                new InternalMsgHandler()
                        );
                    }
                });
        ChannelFuture future = bootstrap.connect(host, port).sync();
        if (!future.isSuccess()) {
            log.warn("连接服务器失败：{}", uri);
            return;
        }

        log.info("连接服务器成功：{}", uri);

        final CountDownLatch latch = new CountDownLatch(5);
        while (!latch.await(200, TimeUnit.MILLISECONDS) && !handshake.isHandshakeComplete()) {
            latch.countDown();
        }

        if (!handshake.isHandshakeComplete()) {
            log.warn("握手失败：{}", uri);
            return;
        }

        log.info("握手成功:{}", uri);

        this.channel = future.channel();
        this.channel.closeFuture().addListener(f -> {
            if (Objects.nonNull(closeCallback)) {
                closeCallback.accept(f);
                log.info("回调关闭事件:{}", uri);
            }
        });
    }

    public void sendMsg(Object msg) {
        if (ObjectUtils.allNotNull(msg, channel)) {
            channel.writeAndFlush(msg);
        }
    }

}
