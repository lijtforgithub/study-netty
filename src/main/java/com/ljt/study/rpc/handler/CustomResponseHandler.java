package com.ljt.study.rpc.handler;

import com.ljt.study.rpc.ResponseCallback;
import com.ljt.study.rpc.protocol.CustomPackage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author LiJingTang
 * @date 2021-03-06 21:41
 */
public class CustomResponseHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CustomPackage pck = (CustomPackage) msg;
        ResponseCallback.call(pck);
    }

}
