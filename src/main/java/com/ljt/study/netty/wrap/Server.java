package com.ljt.study.netty.wrap;

import com.ljt.study.netty.TimeServer;
import com.ljt.study.netty.handler.TimeServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author LiJingTang
 * @date 2020-04-30 17:12
 */
public class Server {

    public static void main(String[] args) {
        new TimeServer(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new TimeServerHandler());
            }
        });
    }

}
