package com.ljt.study.game.handler;

import com.ljt.study.game.model.HandlerContext;
import com.ljt.study.game.msg.EntryMsg;
import com.ljt.study.game.util.LoadStatistics;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import static com.ljt.study.gateway.core.SessionManage.KEY_GATEWAY_ID;

/**
 * @author LiJingTang
 * @date 2022-04-29 16:21
 */
@Slf4j
public class EntryMsgHandler implements MsgHandler<EntryMsg> {

    @Override
    public void handle(HandlerContext context, EntryMsg msg) {
        Integer gatewayId = context.getChannelContext().channel().attr(AttributeKey.<Integer>valueOf(KEY_GATEWAY_ID)).get();
        LoadStatistics.addUser(gatewayId, context.getUserId());
        log.info("新人入场{} {}", gatewayId, context.getUserId());
    }

}
