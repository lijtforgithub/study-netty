package com.ljt.study.netty.protocol.custom;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2020-05-11 11:17
 */
@Slf4j
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(buildLoginReq());
    }

    private NettyMessage buildLoginReq() {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        message.setHeader(header);
        header.setType(MessageTyeEnum.LOGIN_REQ.getValue());
        message.setBody((byte) 0);
        return message;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        NettyMessage message = (NettyMessage) msg;
        // 如果是握手应答消息 判断是否认证成功
        if (Objects.nonNull(message.getHeader())
                && MessageTyeEnum.LOGIN_RESP.getValue() == message.getHeader().getType()) {
            byte loginResult = (byte) message.getBody();
            if (loginResult != (byte) 0) {
                // 握手失败 关闭连接
                ctx.close();
            } else {
                log.info("Login is ok : {}", message);
                ctx.fireChannelRead(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
    }

}
