package com.ljt.study.netty.decoder;

import com.ljt.study.netty.Server;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author LiJingTang
 * @date 2020-05-08 14:47
 */
public class FixedLengthServer {

    public static void main(String[] args) {
        new Server(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(new FixedLengthFrameDecoder(20))
                        .addLast(new StringDecoder())
                        .addLast(new EchoServerHandler());
            }
        }).start();
    }

    @Slf4j
    private static class EchoServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String body = (String) msg;
            log.info("服务端接收到消息：{}", body);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error(StringUtils.EMPTY, cause);
            ctx.close();
        }
    }

}
