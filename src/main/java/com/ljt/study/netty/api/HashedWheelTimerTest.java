package com.ljt.study.netty.api;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author LiJingTang
 * @date 2025-01-14 10:34
 */
public class HashedWheelTimerTest {

    public static void main(String[] args) {
        Timer timer = new HashedWheelTimer();

        Timeout timeout1 = timer.newTimeout(timeout -> System.out.println("timeout1: " + new Date()), 10, TimeUnit.SECONDS);

        if (!timeout1.isExpired()) {
            timeout1.cancel();
        }

        timer.newTimeout(timeout -> {
            System.out.println("timeout2: " + new Date());
            // 时间轮中的任务执行是串行的，当一个任务执行的时间过长，会影响后续任务的调度和执行 ScheduledExecutorService单个线程也是一样的效果
            Thread.sleep(5000);
        }, 1, TimeUnit.SECONDS);

        timer.newTimeout(timeout -> System.out.println("timeout3: " + new Date()), 3, TimeUnit.SECONDS);

        timer.stop();
    }

}
