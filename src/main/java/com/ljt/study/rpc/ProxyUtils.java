package com.ljt.study.rpc;

import com.ljt.study.rpc.protocol.ProtocolManage;
import com.ljt.study.rpc.protocol.RequestBody;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.ljt.study.rpc.RpcUtils.createRequestBody;

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
                log.debug("LC: Local Call");
                return method.invoke(obj, args);
            } else {
                log.debug("RPC: Remote Procedure Call");
                RequestBody requestBody = createRequestBody(clazz, method, args);
                CompletableFuture<Object> future = ProtocolManage.getTransporter().transport(requestBody);

                return future.get();
            }
        });

        return result;
    }

}
