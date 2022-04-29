package com.ljt.study.gateway.core;

import com.ljt.study.game.enums.MsgTypeEnum;
import com.ljt.study.game.processor.AsyncProcessor;
import com.ljt.study.game.util.DLock;
import com.ljt.study.game.util.RedisPubSub;
import com.ljt.study.game.util.RedisUtils;
import com.ljt.study.gateway.GatewayServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author LiJingTang
 * @date 2022-04-27 13:28
 */
@Slf4j
public class CheckLoginHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }

        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = frame.content();
        int sessionId = byteBuf.readInt();
        int userId = byteBuf.readInt();
        log.info("返回sessionId={} userId={}", sessionId, userId);

        short type = byteBuf.readShort();
        boolean isLoginCmd = MsgTypeEnum.LOGIN == MsgTypeEnum.getEnum(type);

        byteBuf.resetReaderIndex();

        // 在返回消息的时候处理用户是否重复登录原因是 有可能是多种方式的登录 请求参数不一样 但是返回消息可以一样
        if (isLoginCmd) {
            Channel channel = SessionManage.getChannelBySessionId(sessionId);
            if (Objects.isNull(channel)) {
                return;
            }

            Channel existChannel = SessionManage.getChannelByUserId(userId);
            if (Objects.nonNull(existChannel)) {
                SessionManage.sendMsg(ctx, "您已经登录：" + RedisUtils.getUser(userId));
                // 两个连接连到一个网关 都断开 方便处理
                existChannel.disconnect().sync();
                channel.disconnect().sync();
            }

            // 因为操作Redis也是IO操作 所以放到异步线程池里
            AsyncProcessor.process(userId, getSupplier(userId, channel),
                    b -> ctx.executor().submit(() -> {
                        if (channel.isOpen()) {
                            ctx.fireChannelRead(msg);
                        }
                    }));
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private Supplier<Void> getSupplier(int userId, Channel channel) {
        return () -> {
            try (DLock dLock = DLock.tryLockAndGet("dlock:user:login:" + userId, 2000)) {
                if (null == dLock) {
                    log.warn("分布式锁加锁失败：userId = {}", userId);
                    channel.disconnect().sync();
                }

                if (checkForRedis(userId)) {
                    SessionManage.setUserId(channel, userId);
                    try (Jedis jedis = RedisUtils.getJedis()) {
                        jedis.sadd(GatewayServer.getServerKey(null), String.valueOf(userId));
                    }
                } else {
                    log.warn("发生重复登录 断开连接：userId = {}", userId);
                    channel.disconnect().sync();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            return null;
        };
    }

    private boolean checkForRedis(Integer userId) {
        try (Jedis jedis = RedisUtils.getJedis()) {
            String serverId = RedisUtils.getGatewayId(userId);
            // redis用户信息里没有登录网关的信息
            if (StringUtils.isBlank(serverId)) {
                return RedisUtils.setNxGatewayId(userId, GatewayServer.getId());
            }

            // redis用户信息记录登录的就是当前网关
            if (GatewayServer.getId().equals(serverId)) {
                return true;
            }

            // 判断网关服务记录登录用户是否存在当前用户ID
            Boolean exist = jedis.sismember(GatewayServer.getServerKey(serverId), String.valueOf(userId));
            if (Boolean.TRUE.equals(exist)) {
                // 发布登出信息 让对应的网关服务器处理
                RedisPubSub.pubLogout(serverId, userId);
                // 这里返回 false 让自己的网关主动断开 但是当前网关并没有返回登录成功 还要再次登录才可以
                return false;
            }

            // 服务器宕机 网关的key会删掉 没有续约 但是用户信息里记录的网关ID还在
            RedisUtils.setGatewayId(userId, GatewayServer.getId());

            return true;
        } catch (Exception e) {
            log.error("校验重复登录", e);
            return false;
        }
    }

}
