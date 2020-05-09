package com.ljt.study.netty.serialize;

import com.ljt.study.netty.Server;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author LiJingTang
 * @date 2020-05-08 16:06
 */
public class MarshallingServer {

    public static void main(String[] args) {
        new Server(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(MarshallingFactory.buildDecoder())
                        .addLast(MarshallingFactory.buildEncoder())
                        .addLast(new SubReqServerHandler());
            }
        }).start();
    }

}
