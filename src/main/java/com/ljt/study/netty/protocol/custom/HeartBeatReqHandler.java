package com.ljt.study.netty.protocol.custom;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author LiJingTang
 * @date 2020-05-11 13:37
 */
@Slf4j
public class HeartBeatReqHandler extends ChannelHandlerAdapter {

    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("HeartBeatReqHandler->channelRead");
        NettyMessage message = (NettyMessage) msg;
        // 握手成功 主动发送心跳消息
        if (Objects.nonNull(message.getHeader())
                && MessageTyeEnum.LOGIN_RESP.getValue() == message.getHeader().getType()) {
            heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 0, 5, TimeUnit.SECONDS);
        } else if (Objects.nonNull(message.getHeader())
                && MessageTyeEnum.HEARTBEAT_RESP.getValue() == message.getHeader().getType()) {
            log.info("Client receive server heart beat message : ---> {}", message);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (Objects.nonNull(heartBeat)) {
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }

    private class HeartBeatTask implements Runnable {

        private final ChannelHandlerContext ctx;

        public HeartBeatTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            NettyMessage heartBeat = buildHeartBeat();
            log.info("Client send heart beat message to server : ---> {}", heartBeat);
            ctx.writeAndFlush(heartBeat);
        }

        private NettyMessage buildHeartBeat() {
            NettyMessage message = new NettyMessage();
            Header header = new Header();
            message.setHeader(header);
            header.setType(MessageTyeEnum.HEARTBEAT_REQ.getValue());
            return message;
        }
    }

}
