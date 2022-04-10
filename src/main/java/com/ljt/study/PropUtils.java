package com.ljt.study;

import java.io.IOException;
import java.util.Properties;

/**
 * @author LiJingTang
 * @date 2022-04-10 20:15
 */
public final class PropUtils {

    private PropUtils() {}

    private static final Properties CONFIG_PROP = new Properties();

    static {
        try {
            CONFIG_PROP.load(PropUtils.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getNacosServer() {
        return CONFIG_PROP.getProperty("nacos.server");
    }

    public static String getServiceName() {
        return CONFIG_PROP.getProperty("service.name");
    }

}
