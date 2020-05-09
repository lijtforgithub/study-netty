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

    private static final String SERIAL = "serial";

    public static MarshallingDecoder buildDecoder() {
        final MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory(SERIAL);
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        return new MarshallingDecoder(new DefaultUnmarshallerProvider(factory, configuration), 1024);
    }

    public static MarshallingEncoder buildEncoder() {
        final MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory(SERIAL);
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        return new MarshallingEncoder(new DefaultMarshallerProvider(factory, configuration));
    }

}
