package com.ljt.study.rpc.protocol;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author LiJingTang
 * @date 2021-03-06 16:30
 */
@Setter
@Getter
public class CustomResponseBody implements Serializable {

    private static final long serialVersionUID = 3081721586643533074L;

    private Object result;

}
