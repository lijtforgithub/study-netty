package com.ljt.study.rpc.transport;

import com.ljt.study.rpc.CustomProtocolCallback;
import com.ljt.study.rpc.protocol.CustomHeader;
import com.ljt.study.rpc.protocol.RequestBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import static com.ljt.study.rpc.RpcUtils.*;

/**
 * @author LiJingTang
 * @date 2021-03-07 13:06
 */
public class CustomProtocolTransporter implements Transporter {

    @Override
    public CompletableFuture<Object> transport(String host, int port, RequestBody requestBody) {
        final byte[] body = serial(requestBody);
        CustomHeader customHeader = createHeader(body.length);
        final byte[] header = serial(customHeader);

        CompletableFuture<Object> future = new CompletableFuture<>();
        CustomProtocolCallback.add(customHeader.getRequestId(), future);

        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(header.length + body.length);
        byteBuf.writeBytes(header).writeBytes(body);
        SocketChannel client = ClientFactory.getClient(new InetSocketAddress(host, port));
        client.writeAndFlush(byteBuf);

        return future;
    }

}
