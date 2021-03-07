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
     * @param host        主机
     * @param port        端口号
     * @param requestBody 请求参数
     * @return 异步响应
     * @throws Exception 发送异常
     */
    CompletableFuture<Object> transport(String host, int port, RequestBody requestBody) throws Exception;

}
