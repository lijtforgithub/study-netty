package com.ljt.study.netty.protocol.custom;

import com.google.common.collect.Maps;
import com.ljt.study.netty.serialize.MarshallingFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author LiJingTang
 * @date 2020-05-11 16:37
 */
@Slf4j
public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {

    private MarshallerDecoder marshallerDecoder;

    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
        marshallerDecoder = new MarshallerDecoder();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (Objects.isNull(frame)) {
            log.warn("frame is null");
            return null;
        }

        NettyMessage message = new NettyMessage();
        Header header = new Header();
        message.setHeader(header);
        header.setCrcCode(frame.readInt());
        header.setLength(frame.readInt());
        header.setSessionId(frame.readLong());
        header.setType(frame.readByte());
        header.setPriority(frame.readByte());

        int size = frame.readInt();
        if (size > 0) {
            Map<String, Object> attach = Maps.newHashMapWithExpectedSize(size);

            for (int i = 0; i < size; i++) {
                int keySize = frame.readInt();
                byte[] keyArray = new byte[keySize];
                in.readBytes(keyArray);
                String key = new String(keyArray, UTF_8);
                attach.put(key, marshallerDecoder.decode(ctx, frame));
            }
            header.setAttachment(attach);
        }

        if (in.readableBytes() > 0) {
            message.setBody(marshallerDecoder.decode(ctx, frame));
        }

        return message;
    }
}

final class MarshallerDecoder extends MarshallingDecoder {

    public MarshallerDecoder() {
        super(MarshallingFactory.getUnmarshallerProvider(), 1024);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        return super.decode(ctx, in);
    }
}