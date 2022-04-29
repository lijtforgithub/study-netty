package com.ljt.study.netty.decoder;

import com.ljt.study.netty.Client;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.ljt.study.Constant.DELIMITER;

/**
 * @author LiJingTang
 * @date 2020-05-08 14:10
 */
public class DelimiterBasedClient {

    public static void main(String[] args) {
        new Client(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ByteBuf delimiter = Unpooled.copiedBuffer(DELIMITER.getBytes());
                socketChannel.pipeline()
                        .addLast(new DelimiterBasedFrameDecoder(1024, delimiter))
                        .addLast(new StringDecoder())
                        .addLast(new EchoClientHandler());
            }
        }).start();
    }

    @Slf4j
    private static class EchoClientHandler extends ChannelInboundHandlerAdapter {

        private int counter = 0;
        private final String ECHO_REQ = "Hi, LiJingTang. Welcome to Netty." + DELIMITER;

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            for (int i = 0; i < 10; i++) {
                ctx.writeAndFlush(Unpooled.copiedBuffer(ECHO_REQ.getBytes()));
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String body = (String) msg;
            log.info("第 {} 次接收到服务端消息：{}", ++counter, body);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error(StringUtils.EMPTY, cause);
            ctx.close();
        }
    }

}
