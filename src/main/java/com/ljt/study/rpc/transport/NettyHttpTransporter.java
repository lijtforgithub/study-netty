package com.ljt.study.rpc.transport;

import com.ljt.study.rpc.protocol.RequestBody;

import java.util.concurrent.CompletableFuture;

/**
 * @author LiJingTang
 * @date 2021-03-07 14:14
 */
public class NettyHttpTransporter implements Transporter {

    @Override
    public CompletableFuture<Object> transport(RequestBody requestBody) {
        return null;
    }

}
