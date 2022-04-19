package com.ljt.study.game.processor;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
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

    private AsyncProcessor() {
    }

    private static final Executor[] EXECUTOR = new Executor[5];

    static {
        for (int i = 0; i < EXECUTOR.length; i++) {
            int finalI = i;
            EXECUTOR[i] = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setName(finalI + ":async-processor");
                return thread;
            });
        }
    }

    public static <T> void process(final int bindId, final Supplier<T> supplier, final Consumer<T> consumer) {
        log.info("提交异步任务");

        int i = Math.abs(bindId) % EXECUTOR.length;
        Executor executor = EXECUTOR[i];

        executor.execute(() -> {
            T t = supplier.get();
            if (Objects.nonNull(consumer)) {
                MainProcessor.process(() -> consumer.accept(t));
            }
        });
    }

}
