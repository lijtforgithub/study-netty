package com.ljt.study.rpc;

import com.ljt.study.rpc.protocol.CustomHeader;
import com.ljt.study.rpc.protocol.CustomRequestBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.ljt.study.rpc.RpcUtils.*;

/**
 * @author LiJingTang
 * @date 2021-03-06 17:01
 */
@Slf4j
public class ProxyUtils {

    private ProxyUtils() {
    }

    public static <T> T generate(Class<T> clazz) {
        ClassLoader classLoader = ProxyUtils.class.getClassLoader();
        Class<?>[] interfaces = new Class[]{clazz};
        @SuppressWarnings("unchecked")
        T result = (T) Proxy.newProxyInstance(classLoader, interfaces, (Object proxy, Method method, Object[] args) -> {
            // 本地调用 同一个JVM
            final Object obj = Dispatcher.get(clazz);
            if (Objects.nonNull(obj)) {
                log.info("LC: Local call");
                return method.invoke(obj, args);
            } else {
                log.info("RPC call");
                CustomRequestBody requestBody = createRequestBody(clazz, method, args);
                final byte[] body = RpcUtils.serial(requestBody);
                CustomHeader customHeader = createHeader(body.length);
                final byte[] header = serial(customHeader);

                CompletableFuture<Object> future = new CompletableFuture<>();
                ResponseCallback.add(customHeader.getRequestId(), future);

                ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(header.length + body.length);
                byteBuf.writeBytes(header).writeBytes(body);
                SocketChannel client = ClientFactory.getClient(ADDRESS);
                client.writeAndFlush(byteBuf);

                return future.get();
            }
        });

        return result;
    }


}
