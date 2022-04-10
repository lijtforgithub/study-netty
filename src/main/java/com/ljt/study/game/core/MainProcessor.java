package com.ljt.study.game.core;

import com.ljt.study.game.handler.MsgHandler;
import com.ljt.study.game.msg.BaseMsg;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author LiJingTang
 * @date 2022-04-10 13:42
 */
@Slf4j
public final class MainProcessor {

    private MainProcessor() {
    }

    private static final Executor EXECUTOR;

    static {
        EXECUTOR = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("main-processor");
            return thread;
        });
    }

    public static void process(MsgHandler<?> handler, ChannelHandlerContext ctx, BaseMsg msg) {
        log.info("提交主任务: {}", msg);
        EXECUTOR.execute(() -> {
            HandlerContext context = new HandlerContext(ctx);
            handler.handle(context, cast(msg));
        });
    }

    public static void process(Runnable r) {
        log.info("提交主任务R");
        EXECUTOR.execute(r);
    }

    private static <T extends BaseMsg> T cast(BaseMsg msg) {
        return (T) msg;
    }

}
