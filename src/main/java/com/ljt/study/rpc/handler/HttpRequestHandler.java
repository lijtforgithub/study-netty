package com.ljt.study.rpc.handler;

import com.ljt.study.rpc.protocol.RequestBody;
import com.ljt.study.rpc.protocol.ResponseBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import static com.ljt.study.rpc.RpcUtils.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

/**
 * @author LiJingTang
 * @date 2021-03-07 14:44
 */
@Slf4j
public class HttpRequestHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;
        log.debug(request.toString());

        ByteBuf byteBuf = request.content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        RequestBody requestBody = unSerialRequestBody(bytes);
        Object result = invoke(requestBody.getTypeName(), requestBody.getMethodName(),
                requestBody.getParameterTypes(), requestBody.getArgs());
        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        final byte[] body = serial(responseBody);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.OK, Unpooled.copiedBuffer(body));

        // netty-http 是必须
        response.headers().set(CONTENT_LENGTH, body.length);
        ctx.writeAndFlush(response);
    }

}
