package com.ljt.study.netty.serialize;

import com.ljt.study.netty.Client;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author LiJingTang
 * @date 2020-05-08 15:48
 */
public class JavaClient {

    public static void main(String[] args) {
        new Client(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(new ObjectDecoder(1024 * 1024,
                                ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())))
                        .addLast(new ObjectEncoder())
                        .addLast(new SubReqClientHandler());
            }
        }).start();
    }

}
