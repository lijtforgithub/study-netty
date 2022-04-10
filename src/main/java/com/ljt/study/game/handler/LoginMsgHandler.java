package com.ljt.study.game.handler;

import com.ljt.study.game.core.HandlerContext;
import com.ljt.study.game.msg.LoginMsg;
import com.ljt.study.game.service.LoginService;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * @author LiJingTang
 * @date 2022-04-10 11:28
 */
@Slf4j
public class LoginMsgHandler implements MsgHandler<LoginMsg> {

    @Override
    public void handle(HandlerContext context, LoginMsg msg) {
        log.info("用户ID：{} 密码：{} 内容：{}", msg.getUserId(), msg.getPassword(), msg.getContent());

        Consumer<LoginMsg> consumer = context::writeAndFlush;

        LoginService.getInstance().login(msg.getUserId(), msg.getPassword(), consumer);
    }

}
