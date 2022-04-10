package com.ljt.study.game.handler;

import com.ljt.study.game.core.HandlerContext;
import com.ljt.study.game.msg.BaseMsg;

/**
 * @author LiJingTang
 * @date 2022-04-10 11:18
 */
public interface MsgHandler<T extends BaseMsg> {

    void handle(HandlerContext context, T msg);

}
