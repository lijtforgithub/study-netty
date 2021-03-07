package com.ljt.study.rpc.protocol;

/**
 * @author LiJingTang
 * @date 2021-03-07 13:10
 */
public enum ProtocolEnum {

    /**
     * 自定义协议
     */
    CUSTOM_RPC,
    /**
     * 阻塞IO + HTTP
     */
    BIO_HTTP,
    /**
     * netty + HTTP
     */
    NETTY_HTTP;

}
