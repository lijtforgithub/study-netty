package com.ljt.study.gateway;

import com.ljt.study.game.util.RedisUtils;
import com.ljt.study.gateway.core.ClientMsgHandler;
import com.ljt.study.game.util.RedisPubSub;
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
import org.apache.commons.cli.*;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author LiJingTang
 * @date 2022-04-10 21:29
 */
@Slf4j
public class GatewayServer {

    private static final int DEF_PORT = 12345;
    private static CommandLine cmdLine;
    private static String id;

    public static String getServerKey(String serverId) {
        return "game:gateway:" + Optional.ofNullable(serverId).orElse(id);
    }

    public static String getId() {
        return id;
    }

    public static void main(String[] args) throws ParseException {
        initCmdLine(args);
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

            int port = getPort();
            ChannelFuture future = bootstrap.bind(port).sync();

            if (future.isSuccess()) {
                log.info("服务启动成功：{} ", port);
                renew();
                RedisPubSub.subLogout();
                ServiceDiscovery.findService();
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

    private static void initCmdLine(String[] args) throws ParseException {
        Options ops = new Options();
        String id = "id";
        ops.addOption(id, id, true, "网关ID");
        ops.addOption("p", "port", true, "端口号");

        CommandLineParser parser = new DefaultParser();
        cmdLine = parser.parse(ops, args);
        GatewayServer.id = cmdLine.getOptionValue(id, String.valueOf(getPort()));
    }

    private static int getPort() {
        return Integer.parseInt(cmdLine.getOptionValue("p", String.valueOf(DEF_PORT)));
    }

    private static void renew() {
        // 定时续约 服务器宕机 记录登录的用户信息自动消失 防止一直登录不了
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try (Jedis jedis = RedisUtils.getJedis()) {
                jedis.expire(getServerKey(null), 8);
            } catch (Exception e) {
                log.error("续约失败", e);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

}
