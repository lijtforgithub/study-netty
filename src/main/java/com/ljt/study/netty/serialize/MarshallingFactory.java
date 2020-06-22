package com.ljt.study.netty.serialize;

import io.netty.handler.codec.marshalling.*;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

/**
 * @author LiJingTang
 * @date 2020-05-08 16:18
 */
public final class MarshallingFactory {

    private MarshallingFactory() {
    }

    private static final String SERIAL = "serial";

    public static UnmarshallerProvider getUnmarshallerProvider() {
        final MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory(SERIAL);
        return new DefaultUnmarshallerProvider(factory, getConfiguration());
    }

    public static MarshallerProvider getMarshallerProvider() {
        final MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory(SERIAL);
        return new DefaultMarshallerProvider(factory, getConfiguration());
    }

    private static MarshallingConfiguration getConfiguration() {
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        return configuration;
    }

    public static MarshallingDecoder buildDecoder() {
        return new MarshallingDecoder(getUnmarshallerProvider(), 1024);
    }

    public static MarshallingEncoder buildEncoder() {
        return new MarshallingEncoder(getMarshallerProvider());
    }

}
