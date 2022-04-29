package com.ljt.study.game.handler;

import com.ljt.study.game.core.ChannelManage;
import com.ljt.study.game.model.HandlerContext;
import com.ljt.study.game.msg.AttackMsg;
import lombok.extern.slf4j.Slf4j;

/**
 * @author LiJingTang
 * @date 2022-04-10 11:28
 */
@Slf4j
public class AttackMsgHandler implements MsgHandler<AttackMsg> {

    @Override
    public void handle(HandlerContext context, AttackMsg msg) {
        log.info("目标用户ID：{} 血量：{} 内容：{}", msg.getTargetUserId(), msg.getHp(), msg.getContent());

        AttackMsg newMsg = new AttackMsg();
        newMsg.setTargetUserId(msg.getTargetUserId());
        newMsg.setContent("当前服广播攻击成功：" + msg.getHp());

        ChannelManage.broadcastCurrentServer(newMsg);
    }

}
