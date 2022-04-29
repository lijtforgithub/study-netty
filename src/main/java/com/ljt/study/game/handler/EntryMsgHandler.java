package com.ljt.study.game.handler;

import com.ljt.study.game.core.ChannelUserManage;
import com.ljt.study.game.model.HandlerContext;
import com.ljt.study.game.msg.EntryMsg;
import lombok.extern.slf4j.Slf4j;

/**
 * @author LiJingTang
 * @date 2022-04-29 16:21
 */
@Slf4j
public class EntryMsgHandler implements MsgHandler<EntryMsg> {

    @Override
    public void handle(HandlerContext context, EntryMsg msg) {
        EntryMsg entryMsg = new EntryMsg();
        entryMsg.setContent("全服广播我来了" + context.getUserId());

        ChannelUserManage.broadcastAll(entryMsg);
    }

}
