package com.ljt.study.rpc.handler;

import com.ljt.study.rpc.protocol.CustomHeader;
import com.ljt.study.rpc.protocol.CustomPackage;
import com.ljt.study.rpc.protocol.CustomRequestBody;
import com.ljt.study.rpc.protocol.CustomResponseBody;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.ljt.study.rpc.RpcUtils.*;
import static com.ljt.study.rpc.protocol.CustomHeader.LENGTH;

/**
 * 粘包拆包
 *
 * @author LiJingTang
 * @date 2021-03-06 21:45
 */
public class CustomProtocolDecode extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (in.readableBytes() > LENGTH) {
            byte[] header = new byte[LENGTH];
            // readerIndex 不变
            in.getBytes(in.readerIndex(), header);
            CustomHeader customHeader = unSerialHeader(header);

            // 是否包含整个消息体
            if (in.readableBytes() - LENGTH >= customHeader.getContentLength()) {
                // readerIndex 移动
                in.readBytes(LENGTH);
                byte[] body = new byte[customHeader.getContentLength()];
                in.readBytes(body);

                CustomPackage pck = new CustomPackage();
                pck.setHeader(customHeader);
                // 请求和响应都在用
                if (customHeader.isRequest()) {
                    CustomRequestBody requestBody = unSerialRequestBody(body);
                    pck.setRequestBody(requestBody);
                } else {
                    CustomResponseBody responseBody = unSerialResponseBody(body);
                    pck.setResponseBody(responseBody);
                }
                out.add(pck);
            } else {
                break;
            }
        }
    }

}
