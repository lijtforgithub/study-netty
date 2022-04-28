package com.ljt.study.game.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.SetParams;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LiJingTang
 * @date 2022-04-28 10:30
 */
@Slf4j
public class DLock implements Closeable {

    /**
     * 分布式锁字典
     */
    private static final Map<String, DLock> LOCKER_MAP = new ConcurrentHashMap<>();
    private static final String OK = "ok";

    /**
     * 锁名称
     */
    private final String name;

    /**
     * XXX 注意: long 类型在 32 位系统里赋值的时候不是原子操作, 例如:
     * long L = 1231597929419823102L;
     * 上面这条语句其实是会被拆成两句来执行的, 先给 L 赋值一个低位 int32 数值然后再赋值一次高位 int32 数值...
     * ( 就好比 CPU 读取 AX 寄存器, 先读取低位 AL 寄存器然后再读取高位 AH 寄存器 )
     * 如果不加 volatile 关键字, 无法保证原子操作!
     * 也就是说当有另外一个线程在读取 L 变量的时候, 可能读到的值远远小于 1231597929419823102L 这个数
     * <p>
     * 开始时间
     */
    private volatile long startTime;

    /**
     * 持续时间
     */
    private volatile long duration;

    /**
     * 是否已经解锁
     */
    private volatile boolean unlocked = false;

    /**
     * 类参数构造器
     *
     * @param name 锁名称
     */
    private DLock(String name) {
        this.name = name;
    }

    /**
     * 尝试加锁并返回加锁对象,
     * XXX 注意: 只有加锁成功才返回加锁对象
     *
     * @param name     锁名称
     * @param duration 持续时间, 单位 = 毫秒
     * @return 分布式锁对象
     */
    public static DLock tryLockAndGet(final String name, final long duration) {
        if (StringUtils.isBlank(name) || duration <= 0) {
            return null;
        }

        // 事先清理已经过期的锁
        autoClean();

        DLock newLock = new DLock(name);
        newLock.startTime = System.currentTimeMillis();
        newLock.duration = duration;

        // 先在本地加锁, 然后再到 Redis 里加锁!
        // 如果本地加锁都失败了,
        // 就直接退出吧, 这样会少一次 IO...
        if (Objects.nonNull(LOCKER_MAP.putIfAbsent(name, newLock))) {
            log.warn("本地加锁失败! name = {}", name);
            return null;
        }

        try (Jedis redis = RedisUtils.getJedis()) {
            // 到 Redis 里去加锁,
            // 分成两个步骤:
            // 1、 通过 setnx 来申请加锁;
            // 2、 如果 setnx 申请加锁失败, 说明之前已经有锁, 那么通过 ttl 指令来拿到之前那把锁的过期时间;
            // XXX 注意: 如果我们分别使用:
            //     redis.setnx(...)
            //     redis.ttl(...)
            // 那这样产生两次 IO 调用, 所以可以考虑采用管道的方式来完成这个逻辑
            Pipeline pl = redis.pipelined();

            pl.set(name, "1", new SetParams().nx().px(duration));
            pl.pttl(name);

            List<Object> resultList = pl.syncAndReturnAll();

            if (resultList.size() < 2) {
                LOCKER_MAP.remove(name);
                return null;
            }

            if (OK.equalsIgnoreCase(String.valueOf(resultList.get(0)))) {
                newLock.startTime = System.currentTimeMillis();
                return newLock;
            } else {
                // 如果 Redis 中已经有锁了,
                // 从 Redis 中同步锁的过期时间到本地
                newLock.startTime = System.currentTimeMillis();
                newLock.duration = (Long) resultList.get(1);
                log.warn("Redis 加锁失败, 将 Redis 锁同步到本地! name = {}", name);
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 解锁,
     * 一把锁只有这么一个操作! 而且解锁逻辑只能执行一次...
     * 这样也就避免了反复调用 tryLock 和 unlock 函数导致的 Bug
     */
    public void unlock() {
        if (unlocked) {
            return;
        }

        // 设置为已解锁,
        // 这就说明这把锁已经费掉了...
        unlocked = true;

        try (Jedis redis = RedisUtils.getJedis()) {
            redis.del(name);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        LOCKER_MAP.remove(name);
    }

    /**
     * 自动清理已经过期的分布式锁
     */
    private static void autoClean() {
        long nowTime = System.currentTimeMillis();
        LOCKER_MAP.values().removeIf(dLock -> Objects.nonNull(dLock) && nowTime - dLock.startTime > dLock.duration);
    }

    @Override
    public void close() {
        unlock();
    }

}
