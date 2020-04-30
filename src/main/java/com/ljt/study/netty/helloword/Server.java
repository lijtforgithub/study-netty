package com.ljt.study.netty.helloword;

import com.ljt.study.netty.TimeServer;

/**
 * @author LiJingTang
 * @date 2020-04-30 17:01
 */
public class Server {

    public static void main(String[] args) {
        new TimeServer().start();
    }

}
