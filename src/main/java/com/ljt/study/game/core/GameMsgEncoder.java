package com.ljt.study.game.core;

import com.alibaba.fastjson.JSON;
import com.ljt.study.game.enums.MsgTypeEnum;
import com.ljt.study.game.msg.BaseMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author LiJingTang
 * @date 2022-04-09 17:46
 */
@Slf4j
public class GameMsgEncoder extends ChannelHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof BaseMsg)) {
            super.write(ctx, msg, promise);
            return;
        }

        short type = MsgTypeEnum.getValue(msg.getClass());
        if (0 == type) {
            super.write(ctx, msg, promise);
            log.warn("未知消息类型：{}", msg.getClass().getName());
            return;
        }

        String text = JSON.toJSONString(msg);
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeShort(type);
        byteBuf.writeBytes(text.getBytes(StandardCharsets.UTF_8));

        BinaryWebSocketFrame outputFrame = new BinaryWebSocketFrame(byteBuf);
        super.write(ctx, outputFrame, promise);
    }

}
