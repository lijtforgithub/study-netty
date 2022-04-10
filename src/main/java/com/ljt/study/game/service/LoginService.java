package com.ljt.study.game.service;

import com.ljt.study.game.core.AsyncProcessor;
import com.ljt.study.game.msg.LoginMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author LiJingTang
 * @date 2022-04-10 16:17
 */
@Slf4j
public class LoginService {

    private LoginService() {}

    private static final LoginService INSTANCE = new LoginService();

    public static LoginService getInstance() {
        return INSTANCE;
    }

    public void login(Integer userId, String password, Consumer<LoginMsg> consumer) {
        log.info("IO-用户登录：{}", userId);

        Supplier<LoginMsg> supplier = () -> {
            // 查询数据库
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error(password);
                e.printStackTrace();
            }
            LoginMsg msg = new LoginMsg();
            msg.setUserId(userId);
            msg.setContent("登录成功");
            return msg;
        };

        AsyncProcessor.process(supplier, consumer);
    }

}
