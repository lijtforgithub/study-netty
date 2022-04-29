package com.ljt.study.gateway.core;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ljt.study.PropUtils;
import com.ljt.study.game.enums.ServiceTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LiJingTang
 * @date 2022-04-10 22:28
 */
@Slf4j
public final class ServiceDiscovery {

    private static final ConcurrentHashMap<String, NettyClient> MAP = new ConcurrentHashMap<>();
    private static NamingService ns;

    private ServiceDiscovery() {
    }

    public static void findService() {
        try {
            ns = NamingFactory.createNamingService(PropUtils.getNacosServer());
            ServiceTypeEnum[] typeEnums = ServiceTypeEnum.values();

            for (ServiceTypeEnum typeEnum : typeEnums) {
                ns.subscribe(PropUtils.getServiceName(), typeEnum.name(), event -> {
                    if (!(event instanceof NamingEvent)) {
                        return;
                    }

                    NamingEvent nEvent = (NamingEvent) event;
                    List<Instance> instances = nEvent.getInstances();
                    instances.forEach(ServiceDiscovery::connect);
                    log.info("连接服务器完成：{}", MAP);
                });
            }
        } catch (Exception e) {
            log.error("发现服务异常", e);
        }
    }

    private static void connect(Instance instance) {
        log.info("{} 可用状态：{}", instance.getInstanceId(), instance.isEnabled());
        String serviceId = instance.getInstanceId();

        if (!instance.isEnabled() || MAP.containsKey(serviceId)) {
            return;
        }

        try {
            NettyClient client = new NettyClient(serviceId, future -> {
                MAP.remove(serviceId);
                log.info("移除服务器：{} 剩余服务器：{}", serviceId, MAP);
            });
            if (client.connect(instance.getIp(), instance.getPort())) {
                MAP.put(serviceId, client);
            }
        } catch (Exception e) {
            log.error("连接服务失败", e);
        }
    }

    public static NettyClient getClientByType(ServiceTypeEnum typeEnum) {
        if (Objects.isNull(ns)) {
            findService();
        }

        try {
            Instance instance = ns.selectInstances(PropUtils.getServiceName(), typeEnum.name(), true)
                    .stream().max(Comparator.comparing(Instance::getWeight)).orElse(null);
            if (Objects.nonNull(instance)) {
                return MAP.get(instance.getInstanceId());
            }
        } catch (Exception e) {
            log.error("查询实例异常", e);
        }
        return null;
    }

    public static NettyClient getClientById(String instanceId) {
        return MAP.get(instanceId);
    }

}
