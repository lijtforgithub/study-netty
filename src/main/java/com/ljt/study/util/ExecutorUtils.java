package com.ljt.study.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author LiJingTang
 * @date 2020-04-29 09:11
 */
@Slf4j
public class ExecutorUtils {

    private ExecutorUtils() {}

    public static TaskExecutor newExecutor(int size) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.initialize();
        taskExecutor.setCorePoolSize(size);
        taskExecutor.setMaxPoolSize(size * 2);
        taskExecutor.setThreadFactory(new CustomizableThreadFactory("study-netty-"));
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        return taskExecutor;
    }

    public static void sleepSeconds(long i) {
        try {
            TimeUnit.SECONDS.sleep(i);
        } catch (InterruptedException e) {
            log.error(StringUtils.EMPTY, e);
        }
    }

    public static void sleep(long i, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(i);
        } catch (InterruptedException e) {
            log.error(StringUtils.EMPTY, e);
        }
    }

}
