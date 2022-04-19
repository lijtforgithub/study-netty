package com.ljt.study.game.handler;

import com.ljt.study.game.model.HandlerContext;
import com.ljt.study.game.model.User;
import com.ljt.study.game.msg.LoginMsg;
import com.ljt.study.game.service.LoginService;
import com.ljt.study.game.util.RedisUtils;
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

        Consumer<User> consumer = user -> {
            log.info("用户登录成功：{}={}", user.getId(), user.getName());

            LoginMsg loginMsg = new LoginMsg();
            loginMsg.setUserId(user.getId());
            loginMsg.setUserName(user.getName());
            loginMsg.setContent("登录成功");
            context.writeAndFlush(loginMsg);

            RedisUtils.saveUser(user);
        };

        LoginService.getInstance().login(msg.getUserId(), msg.getPassword(), consumer);
    }

}
