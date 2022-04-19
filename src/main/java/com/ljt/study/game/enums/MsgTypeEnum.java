package com.ljt.study.game.enums;

import com.ljt.study.game.msg.AttackMsg;
import com.ljt.study.game.msg.BaseMsg;
import com.ljt.study.game.msg.LoginMsg;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2022-04-09 22:28
 */
@Getter
@AllArgsConstructor
public enum MsgTypeEnum {

    LOGIN(1, LoginMsg.class),
    ATTACK(2, AttackMsg.class);

    private final int value;
    private final Class<? extends BaseMsg> clazz;

    public static Class<? extends BaseMsg> getMsgType(short value) {
        MsgTypeEnum typeEnum = getEnum(value);
        if (Objects.nonNull(typeEnum)) {
            return typeEnum.clazz;
        }

        return null;
    }

    public static MsgTypeEnum getEnum(short value) {
        MsgTypeEnum[] values = MsgTypeEnum.values();
        for (MsgTypeEnum typeEnum : values) {
            if (value == typeEnum.getValue()) {
                return typeEnum;
            }
        }

        return null;
    }

    public static short getValue(Class<?> clazz) {
        MsgTypeEnum[] values = MsgTypeEnum.values();
        for (MsgTypeEnum typeEnum : values) {
            if (clazz == typeEnum.getClazz()) {
                return (short) typeEnum.value;
            }
        }

        return 0;
    }

}
