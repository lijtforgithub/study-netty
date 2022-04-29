package com.ljt.study.netty.serialize;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.ljt.study.Constant.LIJT;

/**
 * @author LiJingTang
 * @date 2020-05-08 16:14
 */
@Slf4j
@ChannelHandler.Sharable
public class SubReqServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        SubscribeReq req = (SubscribeReq) msg;
        log.info("服务端接收到消息：{}", req);
        if (LIJT.equalsIgnoreCase(req.getUserName())) {
            ctx.writeAndFlush(resp(req.getSubReqId()));
        }
    }

    private SubscribeResp resp(int subReqId) {
        SubscribeResp resp = new SubscribeResp();
        resp.setSubReqId(subReqId);
        resp.setRespCode(0);
        resp.setDesc("Netty book order succeed, 3 days later, sent to the designated address");
        return resp;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(StringUtils.EMPTY, cause);
        ctx.close();
    }

}