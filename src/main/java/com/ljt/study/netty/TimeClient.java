package com.ljt.study.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

import static com.ljt.study.Constant.*;

/**
 * @author LiJingTang
 * @date 2020-04-30 17:21
 */
@Slf4j
@AllArgsConstructor
public class TimeClient {

    public TimeClient() {
        this(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new TimeClientHandler());
            }
        });
    }

    public TimeClient(ChannelHandler channelHandler) {
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

    @Slf4j
    private static class TimeClientHandler extends ChannelHandlerAdapter {

        private final ByteBuf firstMessage;

        public TimeClientHandler() {
            byte[] req = TIMER_ORDER.getBytes();
            firstMessage = Unpooled.buffer(req.length);
            firstMessage.writeBytes(req);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error(StringUtils.EMPTY, cause);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(firstMessage);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf byteBuf = (ByteBuf) msg;
            log.info("客户端接收到数据：{}", byteBuf.toString(StandardCharsets.UTF_8));
        }
    }

}
