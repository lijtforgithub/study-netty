package com.ljt.study.rpc.protocol;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author LiJingTang
 * @date 2021-03-06 16:17
 */
@Setter
@Getter
public class CustomHeader implements Serializable {

    /**
     * 自定义协议对象序列化后的长度 如果包结构发生变动或增减属性 大小可能会变化
     */
    public static final int LENGTH = 111;

    private static final long serialVersionUID = -7265379079086584564L;

    private boolean request;
    private long requestId;
    private int contentLength;

}
