package com.ljt.study.netty.protocol.custom;

import com.ljt.study.netty.serialize.MarshallingFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author LiJingTang
 * @date 2020-05-11 16:37
 */
public class NettyMessageEncoder extends MessageToMessageEncoder<NettyMessage> {

    private MarshallerEncoder marshallerEncoder;

    public NettyMessageEncoder() {
        this.marshallerEncoder = new MarshallerEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg, List<Object> out) throws Exception {
        System.err.println("encode : " + msg);
        if (!ObjectUtils.allNotNull(msg, msg.getHeader())) {
            throw new Exception("The encode message is null");
        }

        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(msg.getHeader().getCrcCode());
        buffer.writeInt(msg.getHeader().getLength());
        buffer.writeLong(msg.getHeader().getSessionId());
        buffer.writeByte(msg.getHeader().getType());
        buffer.writeByte(msg.getHeader().getPriority());
        buffer.writeInt(msg.getHeader().getAttachment().size());

        for (Map.Entry<String, Object> entry : msg.getHeader().getAttachment().entrySet()) {
            String key = entry.getKey();
            byte[] keyArray = key.getBytes(UTF_8);
            buffer.writeInt(keyArray.length);
            buffer.writeBytes(keyArray);
            Object value = entry.getValue();
            marshallerEncoder.encode(ctx, value, buffer);
        }

        if (Objects.nonNull(msg.getBody())) {
            marshallerEncoder.encode(ctx, msg.getBody(), buffer);
        } else {
            buffer.writeInt(0);
        }
        // 在第四个字节写入buffer的长度
        buffer.setInt(4, buffer.readableBytes());
        // 传递到下一个handler
        out.add(buffer);
    }
}

final class MarshallerEncoder extends MarshallingEncoder {

    public MarshallerEncoder() {
        super(MarshallingFactory.getMarshallerProvider());
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        super.encode(ctx, msg, out);
    }
}