package com.ljt.study.game.core;

import com.ljt.study.game.msg.BaseMsg;
import lombok.Data;

/**
 * @author LiJingTang
 * @date 2022-04-17 14:38
 */
@Data
public class MsgDTO {

    private Integer sessionId;
    private BaseMsg msg;

}
