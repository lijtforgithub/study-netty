package com.ljt.study.netty.protocol.custom;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2020-05-11 13:55
 */
@Slf4j
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        NettyMessage message = (NettyMessage) msg;
        // 返回心跳应答信息
        if (Objects.nonNull(message.getHeader())
                && MessageTyeEnum.HEARTBEAT_REQ.getValue() == message.getHeader().getType()) {
            log.info("Receiver client heart beat message : ---> {}", message);
            NettyMessage heartBeat = buildHeartBeat();
            log.info("Send heart beat response message to client : ---> {}", heartBeat);
            ctx.writeAndFlush(heartBeat);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private NettyMessage buildHeartBeat() {
        NettyMessage message = new NettyMessage();
        Header header = new Header();
        message.setHeader(header);
        header.setType(MessageTyeEnum.HEARTBEAT_RESP.getValue());
        return message;
    }

}
