package com.ljt.study.netty.helloword;

import com.ljt.study.netty.TimeClient;

/**
 * @author LiJingTang
 * @date 2020-04-30 17:07
 */
public class Client {

    public static void main(String[] args) {
        new TimeClient().start();
    }

}
