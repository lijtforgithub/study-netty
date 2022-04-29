package com.ljt.study.game;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ljt.study.PropUtils;
import com.ljt.study.game.core.*;
import com.ljt.study.game.enums.ServiceTypeEnum;
import com.ljt.study.game.util.RedisPubSub;
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
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.ljt.study.Constant.DEF_PORT;
import static com.ljt.study.Constant.LOOP_IP;

/**
 * @author LiJingTang
 * @date 2022-04-09 16:41
 */
@Slf4j
class GameServer {

    private static final String HOST = "host";
    private static final String PORT = "port";
    private static CommandLine cmdLine;

    public static void main(String[] args) throws ParseException {
        initCmdLine(args);
        MsgHandlerFactory.init();
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
                                    new GameMsgEncoder(), // 增加游戏消息编码器
                                    new GameMsgDecoder(), // 增加游戏消息解码器
                                    new GameMsgHandler()  // 最后在这里处理游戏消息
                            );
                        }
                    });

            int port = getPort();
            ChannelFuture future = bootstrap.bind(port).sync();

            if (future.isSuccess()) {
                log.info("服务启动成功：{} ", port);
                registerServer(LOOP_IP, port);
                RedisPubSub.subOfferLine();
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
        ops.addOption("h", HOST, true, "主机地址");
        ops.addOption("p", PORT, true, "端口号");

        CommandLineParser parser = new DefaultParser();
        cmdLine = parser.parse(ops, args);
    }

    private static int getPort() {
        log.debug("设置端口号：{}", cmdLine.getOptionValue("p"));
        return Integer.parseInt(cmdLine.getOptionValue(PORT, String.valueOf(DEF_PORT)));
    }

    private static void registerServer(String ip, int port) {
        String serverAddress = PropUtils.getNacosServer();
        if (StringUtils.isBlank(serverAddress)) {
            log.warn("Nacos 服务地址为空");
            return;
        }

        try {
            String serviceName = PropUtils.getServiceName();
            NamingService ns = NamingFactory.createNamingService(serverAddress);
            String groupName = port > DEF_PORT ? ServiceTypeEnum.GAME.name() : ServiceTypeEnum.LOGIN.name();

            Instance instance = new Instance();
            instance.setIp(ip);
            instance.setPort(port);

            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    instance.setWeight(2000 - ChannelUserManage.getSize());
                    ns.registerInstance(serviceName, groupName, instance);
                } catch (NacosException e) {
                    log.info("注册服务异常", e);
                }
            }, 0, 5, TimeUnit.SECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    // 即使下线服务 不然短时间内重启 网关监听不到
                    ns.deregisterInstance(serviceName, groupName, ip, port);
                    log.info("下线服务成功");
                } catch (NacosException e) {
                    log.info("下线服务异常", e);
                }
            }));
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

}
