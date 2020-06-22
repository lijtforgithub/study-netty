package com.ljt.study.netty.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadLocalRandom;

import static com.ljt.study.Constant.PORT;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author LiJingTang
 * @date 2020-05-09 16:07
 */
@Slf4j
public class UdpServer {

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ServerHandler());
            bootstrap.bind(PORT).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            log.error(StringUtils.EMPTY, e);
        } finally {
            group.shutdownGracefully();
        }
    }

    @Slf4j
    private static class ServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        private static final String[] DATA = {
                "旧时王谢堂前燕，飞入寻常百姓家。",
                "洛阳亲友如相问，一片冰心在玉壶。",
                "朔风如解意，容易莫摧残",
                "为天地立心，为生民立命，为往圣继绝学，为万世开太平。"
        };

        private String nextQuote() {
            int quoteId = ThreadLocalRandom.current().nextInt(DATA.length);
            return DATA[quoteId];
        }

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) {
            String req = msg.content().toString(UTF_8);
            log.info(req);
            ByteBuf byteBuf = Unpooled.copiedBuffer(nextQuote(), UTF_8);
            ctx.writeAndFlush(new DatagramPacket(byteBuf, msg.sender()));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
            log.error(StringUtils.EMPTY, e);
            ctx.close();
        }
    }

}
