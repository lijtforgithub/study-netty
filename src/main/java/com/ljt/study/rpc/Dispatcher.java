package com.ljt.study.rpc;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LiJingTang
 * @date 2021-03-06 17:16
 */
@Slf4j
public class Dispatcher {

    private Dispatcher() {
    }

    private static final Map<Class<?>, Object> MAP = new ConcurrentHashMap<>();

    public static void register(Class<?> clazz, Object obj) {
        MAP.put(clazz, obj);
        log.info("注册服务：{} = {}", clazz, obj);
    }

    public static Object get(Class<?> clazz) {
        return MAP.get(clazz);
    }

}
