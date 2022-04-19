package com.ljt.study.game.core;

import com.ljt.study.game.model.MsgDTO;
import com.ljt.study.game.msg.BaseMsg;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author LiJingTang
 * @date 2022-04-09 22:04
 */
@Slf4j
public final class ChannelManage {

    private ChannelManage() {}

    private static final ChannelGroup GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void broadcast(BaseMsg msg) {
        MsgDTO dto = new MsgDTO();
        dto.setMsg(msg);
        dto.setSessionId(-1);
        GROUP.writeAndFlush(dto);
    }

    public static void addChannel(Channel channel) {
        GROUP.add(channel);
        log.info("添加Channel: {}", channel.id());
    }

    public static void removeChannel(Channel channel) {
        GROUP.remove(channel);
        log.info("删除Channel: {}", channel.id());
    }

}
