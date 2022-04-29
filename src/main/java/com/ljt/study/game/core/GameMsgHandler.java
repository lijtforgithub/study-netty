package com.ljt.study.game.core;

import com.ljt.study.game.handler.MsgHandler;
import com.ljt.study.game.model.MsgDTO;
import com.ljt.study.game.processor.MainProcessor;
import com.ljt.study.game.util.LoadStatistics;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static com.ljt.study.gateway.core.SessionManage.KEY_GATEWAY_ID;

/**
 * @author LiJingTang
 * @date 2022-04-09 17:47
 */
@Slf4j
public class GameMsgHandler extends SimpleChannelInboundHandler<MsgDTO> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ChannelManage.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ChannelManage.removeChannel(ctx.channel());
        Integer gatewayId = ctx.channel().attr(AttributeKey.<Integer>valueOf(KEY_GATEWAY_ID)).get();
        LoadStatistics.removeGateway(gatewayId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            HttpHeaders headers = ((WebSocketServerProtocolHandler.HandshakeComplete) event).requestHeaders();
            String gatewayId = headers.get(KEY_GATEWAY_ID);
            log.info("有网关连入：{}", gatewayId);

            ctx.channel().attr(AttributeKey.<Integer>valueOf(KEY_GATEWAY_ID)).setIfAbsent(Integer.valueOf(gatewayId));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MsgDTO dto) {
        MsgHandler<?> handler = MsgHandlerFactory.getHandler(dto.getMsg().getClass());
        if (Objects.isNull(handler)) {
            log.warn("无法处理消息：{}", dto.getMsg().getClass().getName());
            return;
        }

        MainProcessor.process(ctx, dto, handler);
    }

}
