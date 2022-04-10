package com.ljt.study.game.core;

import com.alibaba.fastjson.JSON;
import com.ljt.study.game.enums.MsgTypeEnum;
import com.ljt.study.game.msg.BaseMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2022-04-09 17:47
 */
@Slf4j
public class GameMsgDecoder extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BinaryWebSocketFrame)) {
            super.channelRead(ctx, msg);
            return;
        }

        BinaryWebSocketFrame inputFrame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = inputFrame.content();

        // 读取消息类型
        short type = byteBuf.readShort();
        Class<? extends BaseMsg> clazz = MsgTypeEnum.getMsgType(type);
        if (Objects.isNull(clazz)) {
            log.warn("未知消息类型：{}", type);
            return;
        }

        byte[] message = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(message);
        String text = new String(message);
        log.debug("消息：{} => {}", clazz.getSimpleName(), text);

        BaseMsg obj = JSON.parseObject(text, clazz);

        // 管道向下传递
        ctx.fireChannelRead(obj);
    }

}
