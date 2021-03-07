package com.ljt.study.rpc.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.ljt.study.rpc.protocol.ProtocolManage.getClientChannelInitializer;

/**
 * @author LiJingTang
 * @date 2021-03-06 21:01
 */
@Slf4j
public class ClientFactory {

    private ClientFactory() {
    }

    private static final int POOL_SIZE = 2;
    private static final Random RAND = new Random();
    private static final EventLoopGroup GROUP = new NioEventLoopGroup();
    private static final Map<InetSocketAddress, ClientPool> MAP = new ConcurrentHashMap<>();

    public static SocketChannel getClient(InetSocketAddress socketAddress) {
        ClientPool pool = MAP.get(socketAddress);
        if (Objects.isNull(pool)) {
            synchronized (MAP) {
                MAP.computeIfAbsent(socketAddress, k -> new ClientPool(POOL_SIZE));
                pool = MAP.get(socketAddress);
            }
        }

        final int i = RAND.nextInt(POOL_SIZE);
        if (pool.nonClient(i)) {
            synchronized (pool.getLocks()[i]) {
                if (pool.nonClient(i)) {
                    pool.getClients()[i] = createClient(socketAddress, getClientChannelInitializer());
                }
            }
        }

        return pool.getClients()[i];
    }

    public static SocketChannel createClient(InetSocketAddress socketAddress, ChannelInitializer<NioSocketChannel> channelInitializer) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(GROUP)
                    .channel(NioSocketChannel.class)
                    .handler(channelInitializer);

            SocketChannel channel = (SocketChannel) bootstrap.connect(socketAddress).sync().channel();
            log.info("客户端创建成功 {}", channel.localAddress());
            return channel;
        } catch (Exception e) {
            log.error("创建客户端异常", e);
            return null;
        }
    }

}

@Setter
@Getter
class ClientPool {

    private SocketChannel[] clients;
    private Object[] locks;

    public ClientPool(int size) {
        clients = new NioSocketChannel[size];
        locks = new Object[size];
        for (int i = 0; i < size; i++) {
            locks[i] = new Object();
        }
    }

    public boolean nonClient(int i) {
        return Objects.isNull(clients[i]) || !clients[i].isActive();
    }

}
