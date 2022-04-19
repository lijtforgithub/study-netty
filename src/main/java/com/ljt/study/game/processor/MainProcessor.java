package com.ljt.study.game.processor;

import com.ljt.study.game.model.HandlerContext;
import com.ljt.study.game.model.MsgDTO;
import com.ljt.study.game.core.MsgHandlerFactory;
import com.ljt.study.game.handler.MsgHandler;
import com.ljt.study.game.msg.BaseMsg;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
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

    public static void process(ChannelHandlerContext ctx, MsgDTO dto) {
        MsgHandler<?> handler = MsgHandlerFactory.getHandler(dto.getMsg().getClass());
        if (Objects.isNull(handler)) {
            log.warn("无法处理消息：{}", dto.getMsg().getClass().getName());
            return;
        }

        log.info("提交主任务: {}", dto);
        EXECUTOR.execute(() -> {
            HandlerContext context = new HandlerContext(ctx, dto.getSessionId());
            if (Objects.nonNull(dto.getUserId()) && dto.getUserId() > 0) {
                context.setUserId(dto.getUserId());
            }

            handler.handle(context, cast(dto.getMsg()));
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
