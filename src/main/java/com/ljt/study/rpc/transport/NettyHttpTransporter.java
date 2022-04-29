package com.ljt.study.rpc.transport;

import com.ljt.study.rpc.protocol.RequestBody;
import com.ljt.study.rpc.protocol.ResponseBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import static com.ljt.study.rpc.RpcUtils.serial;
import static com.ljt.study.rpc.protocol.ProtocolManage.MAX_CONTENT_LENGTH;
import static com.ljt.study.rpc.transport.ClientFactory.createClient;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

/**
 * @author LiJingTang
 * @date 2021-03-07 14:14
 */
@Slf4j
public class NettyHttpTransporter implements Transporter {

    @Override
    public CompletableFuture<Object> transport(String host, int port, RequestBody requestBody) throws Exception {
        CompletableFuture<Object> future = new CompletableFuture<>();

        SocketChannel client = createClient(new InetSocketAddress(host, port), new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) {
                ch.pipeline().addLast(new HttpClientCodec())
                        .addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH))
                        .addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                FullHttpResponse response = (FullHttpResponse) msg;
                                log.info(response.toString());

                                ByteBuf byteBuf = response.content();
                                byte[] bytes = new byte[byteBuf.readableBytes()];
                                byteBuf.readBytes(bytes);

                                ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes));
                                ResponseBody body = (ResponseBody) input.readObject();
                                future.complete(body.getResult());
                            }
                        });
            }
        });

        byte[] body = serial(requestBody);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0,
                HttpMethod.POST, "/", Unpooled.copiedBuffer(body)
        );

        // netty-http 是必须
        request.headers().set(CONTENT_LENGTH, body.length);
        assert client != null;
        client.writeAndFlush(request).sync();

        return future;
    }

}
