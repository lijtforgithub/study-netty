package com.ljt.study.game.core;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author LiJingTang
 * @date 2022-04-10 16:21
 */
@Slf4j
public final class AsyncProcessor {

    private AsyncProcessor() {}

    private static final Executor EXECUTOR;

    static {
        EXECUTOR = Executors.newFixedThreadPool(5, r -> {
            Thread thread = new Thread(r);
            thread.setName("async-processor");
            return thread;
        });
    }

    public static <T> void process(Supplier<T> supplier, Consumer<T> callback) {
        log.info("提交异步任务");
        EXECUTOR.execute(() -> {
            T t = supplier.get();
            MainProcessor.process(() -> callback.accept(t));
        });
    }

}
