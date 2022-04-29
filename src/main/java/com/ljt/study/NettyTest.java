package com.ljt.study;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.ljt.study.Constant.DEF_PORT;

/**
 * @author LiJingTang
 * @date 2021-03-10 15:40
 */
class NettyTest {

    @Test
    void byteBuf() {
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        System.out.println(byteBuf.readerIndex());
        System.out.println(byteBuf.writerIndex());
    }

    @Test
    void loopExecutor() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        group.execute(() -> {
            for (;;) {
                System.out.println("Hello Netty");
               sleep(1);
            }
        });
        group.execute(() -> {
            for (;;) {
                System.out.println("Hello Loop");
               sleep(1);
            }
        });

        sleep(30);
    }

    @Test
    void client() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        NioSocketChannel client = new NioSocketChannel();
        System.out.println(client.isRegistered());

        ChannelFuture future = client.connect(new InetSocketAddress(DEF_PORT)).sync();
        ByteBuf byteBuf = Unpooled.copiedBuffer("Hello Netty".getBytes());
        client.writeAndFlush(byteBuf).sync();

        future.channel().closeFuture().sync();
    }

    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
