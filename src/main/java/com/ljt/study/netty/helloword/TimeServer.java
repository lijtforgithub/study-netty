package com.ljt.study.netty.helloword;

import com.ljt.study.netty.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.ljt.study.Constant.TIMER_BAD;
import static com.ljt.study.Constant.TIMER_ORDER;

/**
 * @author LiJingTang
 * @date 2020-04-30 17:01
 */
public class TimeServer {

    public static void main(String[] args) {
        new Server(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new TimeServerHandler());
            }
        }).start();
    }

    @Slf4j
    private static class TimeServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] req = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(req);
            String body = new String(req);
            log.info("服务端接收到数据：{}", body);

            String currentTime = TIMER_ORDER.equalsIgnoreCase(body) ? LocalDateTime.now().toString() : TIMER_BAD;
            ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes(StandardCharsets.UTF_8));
            ctx.write(resp);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error(StringUtils.EMPTY, cause);
            ctx.close();
        }
    }

}
