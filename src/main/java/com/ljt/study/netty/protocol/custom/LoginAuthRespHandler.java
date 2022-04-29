package com.ljt.study.netty.protocol.custom;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author LiJingTang
 * @date 2020-05-11 11:31
 */
@Slf4j
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {

    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>();
    private String[] whiteList = {"127.0.0.1"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("LoginAuthRespHandler->channelRead");
        NettyMessage message = (NettyMessage) msg;
        if (Objects.nonNull(message.getHeader())
                && MessageTyeEnum.LOGIN_REQ.getValue() == message.getHeader().getType()) {
            String nodeIndex = ctx.channel().remoteAddress().toString();
            NettyMessage loginResp;

            // 重复登录 拒绝
            if (nodeCheck.containsKey(nodeIndex)) {
                loginResp = buildLoginResp((byte) -1);
            } else {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                boolean match = Stream.of(whiteList).anyMatch(ip::equalsIgnoreCase);
                loginResp = buildLoginResp((byte) (match ? 0 : -1));
                if (match) {
                    nodeCheck.put(nodeIndex, Boolean.TRUE);
                }
                log.info("The login response is : {} body [{}]", loginResp, loginResp.getBody());
                ctx.writeAndFlush(loginResp);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildLoginResp(byte b) {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        message.setHeader(header);
        header.setType(MessageTyeEnum.LOGIN_RESP.getValue());
        message.setBody(b);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 删除缓存
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }

}
