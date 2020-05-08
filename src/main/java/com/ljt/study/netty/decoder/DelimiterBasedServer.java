package com.ljt.study.netty.decoder;

import com.ljt.study.netty.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
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
public class DelimiterBasedServer {

    public static void main(String[] args) {
        new Server(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ByteBuf delimiter = Unpooled.copiedBuffer(DELIMITER.getBytes());
                socketChannel.pipeline()
                        .addLast(new DelimiterBasedFrameDecoder(1024, delimiter))
                        .addLast(new StringDecoder())
                        .addLast(new EchoServerHandler());
            }
        }).start();
    }

    @Slf4j
    private static class EchoServerHandler extends ChannelHandlerAdapter {

        private int counter = 0;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String body = (String) msg;
            log.info("第 {} 次接收到客户端消息：{}", ++counter, body);
            body += DELIMITER;
            ByteBuf resp = Unpooled.copiedBuffer(body.getBytes());
            ctx.writeAndFlush(resp);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error(StringUtils.EMPTY, cause);
            ctx.close();
        }
    }

}
