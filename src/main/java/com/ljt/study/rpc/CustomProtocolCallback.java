package com.ljt.study.rpc;

import com.ljt.study.rpc.protocol.CustomPackage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LiJingTang
 * @date 2021-03-06 16:32
 */
public class CustomProtocolCallback {

    private CustomProtocolCallback() {
    }

    private static final Map<Long, CompletableFuture<Object>> MAP = new ConcurrentHashMap<>();


    public static void add(Long requestId, CompletableFuture<Object> future) {
        MAP.put(requestId, future);
    }

    public static void call(CustomPackage pck) {
        CompletableFuture<Object> future = MAP.get(pck.getHeader().getRequestId());
        future.complete(pck.getResponseBody().getResult());
        MAP.remove(pck.getHeader().getRequestId());
    }

}
