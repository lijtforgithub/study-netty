package com.ljt.study.netty.decoder;

import com.ljt.study.netty.Client;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.ljt.study.Constant.SYS_SEP;
import static com.ljt.study.Constant.TIMER_ORDER;

/**
 * @author LiJingTang
 * @date 2020-04-30 17:12
 */
public class LineBasedClient {

    public static void main(String[] args) {
        new Client(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(new LineBasedFrameDecoder(1024))
                        .addLast(new StringDecoder())
                        .addLast(new TimeClientHandler());
            }
        }).start();
    }

    @Slf4j
    private static class TimeClientHandler extends ChannelInboundHandlerAdapter {

        private int counter;
        private byte[] req;

        public TimeClientHandler() {
            req = (TIMER_ORDER + SYS_SEP).getBytes();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            for (int i = 0; i < 100; i++) {
                ByteBuf message = Unpooled.buffer(req.length);
                message.writeBytes(req);
                ctx.writeAndFlush(message);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String body = (String) msg;
            log.info("客户端接收到数据：{}；counter={}", body, ++counter);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error(StringUtils.EMPTY, cause);
            ctx.close();
        }
    }

}
