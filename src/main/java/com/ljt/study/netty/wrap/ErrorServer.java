package com.ljt.study.netty.wrap;

import com.ljt.study.netty.TimeServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.ljt.study.Constant.*;

/**
 * @author LiJingTang
 * @date 2020-05-08 09:47
 */
public class ErrorServer {

    public static void main(String[] args) {
        new TimeServer(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new TimeServerHandler());
            }
        }).start();
    }

    @Slf4j
    private static class TimeServerHandler extends ChannelHandlerAdapter {

        private int counter;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] req = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(req);
            String body = new String(req, StandardCharsets.UTF_8).substring(0, req.length - SYS_SEP.length());
            log.info("服务端接收到数据：{}；counter={}", body, ++counter);

            String currentTime = TIMER_ORDER.equalsIgnoreCase(body) ? LocalDateTime.now().toString() : TIMER_BAD;
            ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
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

}
