package com.ljt.study.rpc;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author LiJingTang
 * @date 2021-03-06 17:20
 */
@Slf4j
public class Invoker {

    private Invoker() {

    }

    public static Object invoke(String typeName, String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Class<?> clazz = Class.forName(typeName);
            Object obj = Dispatcher.get(clazz);
            Method method = clazz.getMethod(methodName, parameterTypes);
            return method.invoke(obj, args);
        } catch (Exception e) {
            log.error("反射调用方法异常", e);
            return null;
        }
    }

}
