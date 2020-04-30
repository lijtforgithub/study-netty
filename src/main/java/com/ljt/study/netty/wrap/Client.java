package com.ljt.study.netty.wrap;

import com.ljt.study.netty.TimeClient;
import com.ljt.study.netty.handler.TimeClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author LiJingTang
 * @date 2020-04-30 17:12
 */
public class Client {

    public static void main(String[] args) {
        new TimeClient(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new TimeClientHandler());
            }
        });
    }

}
