package com.ljt.study.netty.serialize;

import lombok.Data;

import java.io.Serializable;

/**
 * @author LiJingTang
 * @date 2020-05-08 15:25
 */
@Data
public class SubscribeResp implements Serializable {

    private static final long serialVersionUID = 2744254620093500444L;

    /**
     * 订购编号
     */
    private int subReqId;
    /**
     * 订购结果：0表示成功
     */
    private int respCode;
    /**
     * 描述信息
     */
    private String desc;

}
