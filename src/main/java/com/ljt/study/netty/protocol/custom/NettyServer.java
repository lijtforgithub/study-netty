package com.ljt.study.netty.protocol.custom;

import com.ljt.study.netty.Server;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author LiJingTang
 * @date 2020-05-11 14:03
 */
@Slf4j
public class NettyServer {

    public static void main(String[] args) {
        new Server(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(new NettyMessageDecoder(1024 * 1024, 4, 4, -8, 0))
                        .addLast(new NettyMessageEncoder())
                        .addLast(new ReadTimeoutHandler(50))
                        .addLast(new LoginAuthRespHandler())
                        .addLast(new HeartBeatRespHandler());
            }
        }).start();
    }

}
