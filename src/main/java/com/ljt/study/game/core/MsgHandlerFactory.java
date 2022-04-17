package com.ljt.study.game.core;

import com.ljt.study.game.handler.MsgHandler;
import com.ljt.study.game.msg.BaseMsg;
import com.ljt.study.game.util.PackageUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author LiJingTang
 * @date 2022-04-10 12:54
 */
@Slf4j
public final class MsgHandlerFactory {

    private MsgHandlerFactory() {
    }

    private static final Map<Class<?>, MsgHandler<?>> HANDLER_MAP = new HashMap<>();
    private static final String METHOD_NAME = "handle";

    public static MsgHandler<?> getHandler(Class<?> clazz) {
        return HANDLER_MAP.get(clazz);
    }

    @SneakyThrows
    public static void init() {
        String pck = MsgHandler.class.getPackage().getName();
        Set<Class<?>> classSet = PackageUtils.listSubClazz(pck, false, MsgHandler.class);

        for (Class<?> clazz : classSet) {
            if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0) {
                continue;
            }

            Class<?> msgType = getMsgType(clazz);

            if (Objects.nonNull(msgType)) {
                MsgHandler<?> handler = (MsgHandler<?>) clazz.newInstance();
                HANDLER_MAP.put(msgType, handler);
                log.info("初始化消息处理器：{} = {}", msgType, handler);
            }
        }
    }

    private static Class<?> getMsgType(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            boolean flag = method.isBridge() || method.isSynthetic();
            Class<?>[] paramTypes = method.getParameterTypes();

            if (flag || !(METHOD_NAME.equals(method.getName()) && paramTypes.length == 2
                    && paramTypes[0] == HandlerContext.class
                    && BaseMsg.class.isAssignableFrom(paramTypes[1]))) {
                continue;
            }

            return paramTypes[1];
        }

        return null;
    }

}
