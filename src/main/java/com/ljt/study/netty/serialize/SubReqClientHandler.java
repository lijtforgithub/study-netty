package com.ljt.study.netty.serialize;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static com.ljt.study.Constant.LIJT;

/**
 * @author LiJingTang
 * @date 2020-05-08 16:15
 */
@Slf4j
public class SubReqClientHandler extends ChannelHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        for (int i = 0; i < 10; i++) {
            ctx.write(subReq(i));
        }

        ctx.flush();
        log.info("客户端推送完毕");
    }

    private SubscribeReq subReq(int i) {
        SubscribeReq req = new SubscribeReq();
        req.setAddress("上海市南翔");
        req.setPhoneNumber("15155965310");
        req.setProductName("Netty 权威指南");
        req.setSubReqId(i);
        req.setUserName(LIJT);
        return req;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("客户端接收到消息：{}", msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(StringUtils.EMPTY, cause);
        ctx.close();
    }

}
