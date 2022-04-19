package com.ljt.study.game.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LiJingTang
 * @date 2022-04-09 22:26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginMsg extends BaseMsg {

    private Integer userId;
    private String userName;
    private String password;

}
