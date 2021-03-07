package com.ljt.study.rpc.protocol;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author LiJingTang
 * @date 2021-03-06 16:28
 */
@Setter
@Getter
public class CustomRequestBody implements Serializable {

    private static final long serialVersionUID = 2375165610761914315L;

    private String typeName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] args;

}
