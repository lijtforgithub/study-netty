package com.ljt.study.gateway.core;

import com.ljt.study.game.enums.MsgTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2022-04-28 15:39
 */
@Slf4j
public class ValidateUserHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = frame.content();
        short type = byteBuf.readShort();
        boolean isLoginCmd = MsgTypeEnum.LOGIN == MsgTypeEnum.getEnum(type);
        Integer userId = SessionManage.getUserId(ctx.channel());

        if (isLoginCmd) {
            if (Objects.nonNull(userId)) {
                SessionManage.sendMsg(ctx, "请勿重复登录");
                return;
            }
        } else {
            if (Objects.isNull(userId)) {
                SessionManage.sendMsg(ctx, "请先登录...");
                return;
            }
        }

        byteBuf.resetReaderIndex();
        SessionManage.setMsgType(ctx.channel(), type);
        ctx.fireChannelRead(msg);
    }

}
