package com.ljt.study.game.handler;

import com.ljt.study.game.core.HandlerContext;
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

        msg.setContent("攻击成功");
        context.writeAndFlush(msg);
    }

}
