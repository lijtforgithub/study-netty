package com.ljt.study.rpc;

import com.ljt.study.rpc.protocol.ProtocolEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ljt.study.rpc.RpcUtils.PROTOCOL;

/**
 * @author LiJingTang
 * @date 2021-03-06 16:11
 */
public class RpcTest {

    @BeforeEach
    public void setProtocol() {
        System.setProperty(PROTOCOL, ProtocolEnum.CUSTOM_RPC.name());
    }

    @Test
    public void startProvider() throws InterruptedException {
        Dispatcher.register(HelloService.class, new HelloServiceImpl());
        Dispatcher.register(UserService.class, new UserServiceImpl());
        new RpcServer().start();
    }

    @SneakyThrows
    @Test
    public void testConsumer() {
        int size = 10;
        Thread[] threads = new Thread[size];
        AtomicInteger num = new AtomicInteger(0);

        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                UserService userService = RpcProxy.generate(UserService.class);
                System.out.println(userService.getUser(num.incrementAndGet()));
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }


        System.in.read();
    }

    @Test
    public void testLc() {
        Dispatcher.register(HelloService.class, new HelloServiceImpl());
        HelloService helloService = RpcProxy.generate(HelloService.class);
        System.out.println(helloService.say("同一个JVM调用"));
    }

}

interface HelloService {

    String say(String name);

}

class HelloServiceImpl implements HelloService {

    @Override
    public String say(String name) {
        return "Hello " + name;
    }

}

interface UserService {

    User getUser(int id);

}

class UserServiceImpl implements UserService {

    @Override
    public User getUser(int id) {
        User user = new User();
        user.setId(id);
        user.setName("user_" + id);
        return user;
    }

}

@Getter
@Setter
@ToString
class User implements Serializable {

    private static final long serialVersionUID = -8144201761882965444L;

    private int id;
    private String name;

}
