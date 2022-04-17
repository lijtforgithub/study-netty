package com.ljt.study.game.processor;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author LiJingTang
 * @date 2022-04-17 22:10
 */
public interface AsyncOperation<T> extends Supplier<T>, Consumer<T> {

    Random RAND = new Random();

    default int getBindId() {
        // 实际应该根据用户ID
        return RAND.nextInt(Integer.MAX_VALUE);
    }

}
