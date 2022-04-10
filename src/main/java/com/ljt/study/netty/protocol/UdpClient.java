package com.ljt.study.netty.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;

import static com.ljt.study.Constant.DEF_PORT;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author LiJingTang
 * @date 2020-05-09 16:36
 */
@Slf4j
public class UdpClient {

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ClientHandler());
            Channel channel = bootstrap.bind(0).sync().channel();
            channel.writeAndFlush(
                    new DatagramPacket(Unpooled.copiedBuffer("诗词", UTF_8),
                            new InetSocketAddress("255.255.255.255", DEF_PORT))).sync();
            if (!channel.closeFuture().await(15000)) {
                log.warn("查询超时");
            }
        } catch (InterruptedException e) {
            log.error(StringUtils.EMPTY, e);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Slf4j
    private static class ClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) {
            String response = msg.content().toString(UTF_8);
            log.info(response);
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
            log.error(StringUtils.EMPTY, e);
            ctx.close();
        }
    }

}
