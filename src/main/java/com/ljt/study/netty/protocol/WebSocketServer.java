package com.ljt.study.netty.protocol;

import com.ljt.study.netty.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author LiJingTang
 * @date 2020-05-09 14:39
 */
public class WebSocketServer {

    public static void main(String[] args) {
        new Server(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast("http-codec", new HttpServerCodec())
                        .addLast("aggregator", new HttpObjectAggregator(65536))
                        .addLast("http-chunked", new ChunkedWriteHandler())
                        .addLast("handler", new WebSocketServerHandler());
            }
        }).start();
    }

    @Slf4j
    private static class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

        private static final String SOCKET_URL = "ws://localhost:8080/websocket";
        private WebSocketServerHandshaker handshaker;

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
            // 传统的 HTTP 接入
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            }
            // WebSocket 接入
            else if (msg instanceof WebSocketFrame) {
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
            log.error(StringUtils.EMPTY, e);
            ctx.close();
        }

        private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
            // 判断是否是关闭链路的指令
            if (frame instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), ((CloseWebSocketFrame) frame).retain());
                return;
            }
            // 判断是否是 Ping 消息
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            // 仅支持文本消息 不支持二进制消息
            if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException(String
                        .format("%s frame types not supported", frame.getClass().getName()));
            }

            // 返回应答消息
            String request = ((TextWebSocketFrame) frame).text();
            log.info("{} received {}", ctx.channel(), request);
            ctx.channel().write(new TextWebSocketFrame(request + ", 欢迎使用 Netty WebSocket 服务，现在时刻：" + LocalDateTime.now()));
        }

        private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest msg) {
            // 如果 HTTP 解码失败 返回 HTTP 异常
            if (!msg.getDecoderResult().isSuccess() || (!"websocket".equals(msg.headers().get("Upgrade")))) {
                sendHttpResponse(ctx, msg, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
                return;
            }
            // 构造握手响应返回 本机测试
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(SOCKET_URL, null, false);
            handshaker = wsFactory.newHandshaker(msg);
            if (Objects.isNull(handshaker)) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), msg);
            }
        }

        private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest msg, DefaultFullHttpResponse response) {
            // 返回应答给客户端
            if (response.getStatus().code() != OK.code()) {
                ByteBuf byteBuf = Unpooled.copiedBuffer(response.getStatus().toString(), UTF_8);
                response.content().writeBytes(byteBuf);
                byteBuf.release();
                setContentLength(response, response.content().readableBytes());
            }

            // 如果是非 Keep-Alive 关闭链接
            ChannelFuture future = ctx.channel().writeAndFlush(response);
            if (!isKeepAlive(msg) || response.getStatus().code() != OK.code()) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

}
