package com.ljt.study.game.util;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LiJingTang
 * @date 2022-04-29 14:59
 */
@Slf4j
public final class LoadStatistics {

    private static final Integer INIT_WEIGHT = 2000;
    private static final Map<Integer, Set<Integer>> MAP = new ConcurrentHashMap<>();

    private LoadStatistics() {}

    public static int getWeight() {
        int sum = MAP.values().stream().mapToInt(Set::size).sum();
        return INIT_WEIGHT - sum;
    }

    public static void addUser(Integer gatewayId, Integer userId) {
        if (ObjectUtils.anyNull(gatewayId, userId)) {
            return;
        }

        Set<Integer> set = MAP.get(gatewayId);
        if (Objects.isNull(set)) {
            set = new ConcurrentHashSet<>();
            MAP.put(gatewayId, set);
        }
        set.add(userId);
    }

    public static void removeUser(Integer userId) {
        if (Objects.isNull(userId)) {
            return;
        }

        MAP.values().forEach(set -> set.remove(userId));
    }

    public static void removeGateway(Integer gatewayId) {
        if (Objects.isNull(gatewayId)) {
            return;
        }
        MAP.remove(gatewayId);
    }

}
