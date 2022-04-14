package com.ljt.study.game.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author LiJingTang
 * @date 2022-04-10 22:35
 */
@Getter
@AllArgsConstructor
public enum ServiceTypeEnum {

    LOGIN("登录"),
    GAME("游戏");

    private final String desc;

}
