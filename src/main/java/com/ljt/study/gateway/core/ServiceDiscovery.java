package com.ljt.study.gateway.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ljt.study.PropUtils;
import com.ljt.study.game.enums.ServiceTypeEnum;
import lombok.extern.slf4j.Slf4j;

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
        log.info("开始连接：{} 可用状态：{}", instance.getInstanceId(), instance.isHealthy());
        String serviceId = instance.getInstanceId();

        if (!instance.isHealthy() && MAP.containsKey(serviceId)) {
            return;
        }

        try {
            NettyClient client = new NettyClient(serviceId, future -> {
                MAP.remove(serviceId);
                log.info("移除服务器：{} 剩余服务器：{}", serviceId, MAP);
            });
            client.connect(instance.getIp(), instance.getPort());
            MAP.put(serviceId, client);
            log.info("发现服务：{} {}", instance.getServiceName(), instance.getPort());
        } catch (Exception e) {
            log.error("连接服务失败", e);
        }
    }

    public static NettyClient getClientByType(ServiceTypeEnum typeEnum) {
        if (Objects.isNull(ns)) {
            findService();
        }

        try {
            Instance instance = ns.selectOneHealthyInstance(PropUtils.getServiceName(), typeEnum.name());
            if (Objects.nonNull(instance)) {
                log.info("筛选出的服务器：{}", instance.getInstanceId());
                return MAP.get(instance.getInstanceId());
            }
        } catch (NacosException e) {
            log.error("查询实例异常", e);
        }
        return null;
    }

}
