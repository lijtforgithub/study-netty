package com.ljt.study.rpc.handler;

import com.ljt.study.rpc.protocol.CustomHeader;
import com.ljt.study.rpc.protocol.CustomPackage;
import com.ljt.study.rpc.protocol.RequestBody;
import com.ljt.study.rpc.protocol.ResponseBody;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.ljt.study.rpc.RpcUtils.*;
import static com.ljt.study.rpc.protocol.CustomHeader.LENGTH;

/**
 * 自定义协议的粘包拆包
 *
 * @author LiJingTang
 * @date 2021-03-06 21:45
 */
public class CustomProtocolDecode extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
        while (byteBuf.readableBytes() > LENGTH) {
            byte[] header = new byte[LENGTH];
            // readerIndex 不变
            byteBuf.getBytes(byteBuf.readerIndex(), header);
            CustomHeader customHeader = unSerialHeader(header);

            // 是否包含整个消息体
            if (byteBuf.readableBytes() - LENGTH >= customHeader.getContentLength()) {
                // readerIndex 移动
                byteBuf.readBytes(LENGTH);
                byte[] body = new byte[customHeader.getContentLength()];
                byteBuf.readBytes(body);

                CustomPackage pck = new CustomPackage();
                pck.setHeader(customHeader);
                // 请求和响应都在用
                if (customHeader.isRequest()) {
                    RequestBody requestBody = unSerialRequestBody(body);
                    pck.setRequestBody(requestBody);
                } else {
                    ResponseBody responseBody = unSerialResponseBody(body);
                    pck.setResponseBody(responseBody);
                }
                out.add(pck);
            } else {
                break;
            }
        }
    }

}
