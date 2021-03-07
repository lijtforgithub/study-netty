package com.ljt.study.rpc.handler;

import com.ljt.study.rpc.CustomProtocolCallback;
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
        // 已在CustomProtocolDecode里处理成此对象
        CustomPackage pck = (CustomPackage) msg;
        CustomProtocolCallback.call(pck);
    }

}
