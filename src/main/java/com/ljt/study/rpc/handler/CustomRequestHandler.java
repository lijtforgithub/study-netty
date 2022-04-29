package com.ljt.study.rpc.handler;

import com.ljt.study.rpc.protocol.CustomHeader;
import com.ljt.study.rpc.protocol.CustomPackage;
import com.ljt.study.rpc.protocol.RequestBody;
import com.ljt.study.rpc.protocol.ResponseBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

import static com.ljt.study.rpc.RpcUtils.invoke;
import static com.ljt.study.rpc.RpcUtils.serial;

/**
 * 自定义协议：服务端处理请求 调用接口实现方法
 *
 * @author LiJingTang
 * @date 2021-03-06 22:33
 */
@Slf4j
public class CustomRequestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 已在CustomProtocolDecode里处理成此对象
        CustomPackage pck = (CustomPackage) msg;
        /*
         * 直接在当前方法 处理IO和业务和返回
         * 自己创建线程池
         * 使用netty自己的eventLoop来处理业务及返回
         */
        Executor executor;
//        executor = ctx.executor();
        executor = ctx.executor().parent().next();

        String ioThread = Thread.currentThread().getName();
        executor.execute(() -> {
            log.debug("IO: {} 业务：{}", ioThread, Thread.currentThread().getName());

            RequestBody requestBody = pck.getRequestBody();
            Object result = invoke(requestBody.getTypeName(), requestBody.getMethodName(),
                    requestBody.getParameterTypes(), requestBody.getArgs());
            ResponseBody responseBody = new ResponseBody();
            responseBody.setResult(result);

            final byte[] body = serial(responseBody);
            CustomHeader customHeader = pck.getHeader();
            customHeader.setRequest(false);
            customHeader.setContentLength(body.length);
            final byte[] header = serial(customHeader);

            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(header.length + body.length);
            byteBuf.writeBytes(header);
            byteBuf.writeBytes(body);
            ctx.writeAndFlush(byteBuf);
        });
    }

}
