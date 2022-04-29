package com.ljt.study.netty.decoder;

import com.ljt.study.netty.Server;
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

import java.time.LocalDateTime;

import static com.ljt.study.Constant.*;

/**
 * @author LiJingTang
 * @date 2020-04-30 17:12
 */
public class LineBasedServer {

    public static void main(String[] args) {
        new Server(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(new LineBasedFrameDecoder(1024))
                        .addLast(new StringDecoder())
                        .addLast(new TimeServerHandler());
            }
        }).start();
    }

    @Slf4j
    private static class TimeServerHandler extends ChannelInboundHandlerAdapter {

        private int counter;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String body = (String) msg;
            log.info("服务端接收到数据：{}；counter={}", body, ++counter);

            String currentTime = TIMER_ORDER.equalsIgnoreCase(body) ? LocalDateTime.now().toString() : TIMER_BAD;
            currentTime += SYS_SEP;
            ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
            ctx.writeAndFlush(resp);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error(StringUtils.EMPTY, cause);
            ctx.close();
        }
    }

}
