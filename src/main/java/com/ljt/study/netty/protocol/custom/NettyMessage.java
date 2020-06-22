package com.ljt.study.netty.protocol.custom;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LiJingTang
 * @date 2020-05-11 10:19
 */
@Data
public final class NettyMessage implements Serializable {

    private static final long serialVersionUID = -7846144361256851541L;

    /**
     * 消息头
     */
    private Header header;
    /**
     * 消息体
     */
    private Object body;
}

@Data
final class Header implements Serializable {

    private static final long serialVersionUID = 797846596705315241L;

    /**
     * 协议标识
     */
    private int crcCode = 0xabef0101;
    /**
     * 消息长度
     */
    private int length;
    /**
     * 会话ID
     */
    private long sessionId;
    /**
     * 消息类型
     */
    private byte type;
    /**
     * 消息优先级
     */
    private byte priority;
    /**
     * 附件
     */
    private Map<String, Object> attachment = new HashMap<>();

}


@Getter
enum MessageTyeEnum {
    /**
     * 场景
     */
    LOGIN_REQ(1),
    LOGIN_RESP(2),
    HEARTBEAT_REQ(3),
    HEARTBEAT_RESP(4);

    /**
     * 类型值
     */
    private final byte value;

    MessageTyeEnum(int i) {
        this.value = (byte) i;
    }
}
