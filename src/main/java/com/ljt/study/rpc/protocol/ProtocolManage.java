package com.ljt.study.rpc.protocol;

import com.ljt.study.rpc.handler.CustomProtocolDecode;
import com.ljt.study.rpc.handler.CustomRequestHandler;
import com.ljt.study.rpc.handler.CustomResponseHandler;
import com.ljt.study.rpc.handler.HttpRequestHandler;
import com.ljt.study.rpc.transport.BioHttpTransporter;
import com.ljt.study.rpc.transport.CustomProtocolTransporter;
import com.ljt.study.rpc.transport.NettyHttpTransporter;
import com.ljt.study.rpc.transport.Transporter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;

import static com.ljt.study.rpc.RpcUtils.PROTOCOL;
import static com.ljt.study.rpc.protocol.ProtocolEnum.CUSTOM_RPC;

/**
 * @author LiJingTang
 * @date 2021-03-07 13:18
 */
@Slf4j
public class ProtocolManage {

    private ProtocolManage() {
    }

    private static Transporter transporter;
    private static ChannelInitializer<NioSocketChannel> clientChannelInitializer;
    private static ChannelInitializer<NioSocketChannel> serverChannelInitializer;

    public static final int MAX_CONTENT_LENGTH = 1024 * 512;

    static {
        final String protocolName = System.getProperty(PROTOCOL, CUSTOM_RPC.name());
        log.info("协议：{}", protocolName);

        ProtocolEnum protocol = ProtocolEnum.valueOf(protocolName);
        switch (protocol) {
            case CUSTOM_RPC:
                transporter = new CustomProtocolTransporter();
                clientChannelInitializer = new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast(new CustomProtocolDecode())
                                .addLast(new CustomResponseHandler());
                    }
                };
                serverChannelInitializer = new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        log.info("客户端接入：{}", ch.remoteAddress().getPort());
                        ch.pipeline().addLast(new CustomProtocolDecode())
                                .addLast(new CustomRequestHandler());
                    }
                };
                break;
            case BIO_HTTP:
                transporter = new BioHttpTransporter();
                serverChannelInitializer = new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        log.info("客户端接入：{}", ch.remoteAddress().getPort());
                        ch.pipeline().addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH))
                                .addLast(new HttpRequestHandler());
                    }
                };
                break;
            case NETTY_HTTP:
                transporter = new NettyHttpTransporter();
                serverChannelInitializer = new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        log.info("客户端接入：{}", ch.remoteAddress().getPort());
                        ch.pipeline().addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH))
                                .addLast(new HttpRequestHandler());
                    }
                };
                break;
            default:
                break;
        }
    }

    public static Transporter getTransporter() {
        return transporter;
    }

    public static ChannelInitializer<NioSocketChannel> getClientChannelInitializer() {
        return clientChannelInitializer;
    }

    public static ChannelInitializer<NioSocketChannel> getServerChannelInitializer() {
        return serverChannelInitializer;
    }

}
