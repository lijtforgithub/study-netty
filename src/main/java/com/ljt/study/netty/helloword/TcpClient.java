package com.ljt.study.netty.helloword;

import com.ljt.study.netty.Client;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

import static com.ljt.study.Constant.SYS_SEP;
import static com.ljt.study.Constant.TIMER_ORDER;

/**
 * @author LiJingTang
 * @date 2020-05-08 11:01
 */
public class TcpClient {

    public static void main(String[] args) {
        new Client(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new TimeClientHandler());
            }
        }).start();
    }

    @Slf4j
    private static class TimeClientHandler extends ChannelHandlerAdapter {

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
            ByteBuf byteBuf = (ByteBuf) msg;
            log.info("客户端接收到数据：{}；counter={}", byteBuf.toString(StandardCharsets.UTF_8), ++counter);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error(StringUtils.EMPTY, cause);
            ctx.close();
        }
    }

}
