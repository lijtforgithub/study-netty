package com.ljt.study.netty.serialize;

import com.ljt.study.netty.Client;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author LiJingTang
 * @date 2020-05-08 16:06
 */
public class MarshallingClient {

    public static void main(String[] args) {
        new Client(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(MarshallingFactory.buildDecoder())
                        .addLast(MarshallingFactory.buildEncoder())
                        .addLast(new SubReqClientHandler());
            }
        }).start();
    }

}
