package com.ljt.study.netty;

import com.ljt.study.util.ExecutorUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.TaskExecutor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.ljt.study.Constant.LOCAL_HOST;

/**
 * @author LiJingTang
 * @date 2020-04-28 17:56
 */
@Slf4j
public class HelloWord {

    public static void main(String[] args) {
        int port = 8080;
        Server server = new Server(port);
        Client client = new Client(port, LOCAL_HOST);
        TaskExecutor taskExecutor = ExecutorUtils.newExecutor(2);
        taskExecutor.execute(server);
        ExecutorUtils.sleepSeconds(1);
        taskExecutor.execute(client);

        ExecutorUtils.sleepSeconds(5);
        System.exit(0);
    }

    private static final String ORDER = "QUERY TIME ORDER";
    private static final String BAD = "BAD ORDER";

    @AllArgsConstructor
    private static class Server implements Runnable {

        private int port;

        @Override
        public void run() {
            // 配置服务端的NIO线程组
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childHandler(new ChildChannelHandler());
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
    }

    private static class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) {
            socketChannel.pipeline().addLast(new TimeServerHandler());

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

            String currentTime = ORDER.equalsIgnoreCase(body) ? LocalDateTime.now().toString() : BAD;
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

    @AllArgsConstructor
    private static class Client implements Runnable {

        private int port;
        private String host;

        @Override
        public void run() {
            // 配置客户端NIO线程组
            EventLoopGroup group = new NioEventLoopGroup();

            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast(new TimeClientHandler());
                            }
                        });

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

    @Slf4j
    private static class TimeClientHandler extends ChannelHandlerAdapter {

        private final ByteBuf firstMessage;

        public TimeClientHandler() {
            byte[] req = ORDER.getBytes();
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
