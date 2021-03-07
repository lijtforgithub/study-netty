package com.ljt.study.rpc.transport;

import com.ljt.study.rpc.ClientFactory;
import com.ljt.study.rpc.ResponseCallback;
import com.ljt.study.rpc.protocol.CustomHeader;
import com.ljt.study.rpc.protocol.RequestBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.CompletableFuture;

import static com.ljt.study.rpc.RpcUtils.*;

/**
 * @author LiJingTang
 * @date 2021-03-07 13:06
 */
public class CustomProtocolTransporter implements Transporter {

    @Override
    public CompletableFuture<Object> transport(RequestBody requestBody) {
        final byte[] body = serial(requestBody);
        CustomHeader customHeader = createHeader(body.length);
        final byte[] header = serial(customHeader);

        CompletableFuture<Object> future = new CompletableFuture<>();
        ResponseCallback.add(customHeader.getRequestId(), future);

        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(header.length + body.length);
        byteBuf.writeBytes(header).writeBytes(body);
        SocketChannel client = ClientFactory.getClient(ADDRESS);
        client.writeAndFlush(byteBuf);

        return future;
    }

}
