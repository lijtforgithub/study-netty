package com.ljt.study.rpc.transport;

import com.ljt.study.rpc.protocol.RequestBody;

import java.util.concurrent.CompletableFuture;

/**
 * @author LiJingTang
 * @date 2021-03-07 12:57
 */
public interface Transporter {

    /**
     * 发送请求
     *
     * @param requestBody 请求参数
     * @return 异步响应
     */
    CompletableFuture<Object> transport(RequestBody requestBody);

}
