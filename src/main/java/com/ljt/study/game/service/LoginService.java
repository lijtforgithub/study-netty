package com.ljt.study.game.service;

import com.ljt.study.game.model.User;
import com.ljt.study.game.processor.AsyncProcessor;
import com.ljt.study.game.processor.MainProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

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

    public void login(Integer userId, String password, Consumer<User> consumer) {
        log.info("IO-用户登录：{}", userId);

        Supplier<User> supplier = () -> {
            // 查询数据库
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error(password);
                e.printStackTrace();
            }
            User user = new User();
            user.setId(userId);
            user.setName(RandomStringUtils.randomAlphabetic(3).toUpperCase());
            return user;
        };

        AsyncProcessor.process(userId, supplier,
                user -> MainProcessor.process(() -> consumer.accept(user)));
    }

}
