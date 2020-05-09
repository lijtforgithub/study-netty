package com.ljt.study.netty.serialize;

import lombok.Data;

import java.io.Serializable;

/**
 * @author LiJingTang
 * @date 2020-05-08 15:21
 */
@Data
public class SubscribeReq implements Serializable {

    private static final long serialVersionUID = -200111963592288699L;

    /**
     * 订购编号
     */
    private int subReqId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 订购的产品名称
     */
    private String productName;
    /**
     * 订购者电话号码
     */
    private String phoneNumber;
    /**
     * 订购者家庭住址
     */
    private String address;

}
