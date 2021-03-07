package com.ljt.study.rpc.protocol;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author LiJingTang
 * @date 2021-03-06 16:37
 */
@Setter
@Getter
public class CustomPackage implements Serializable {

    private static final long serialVersionUID = 2517265218072922583L;

    private CustomHeader header;
    private CustomRequestBody requestBody;
    private CustomResponseBody responseBody;

}
