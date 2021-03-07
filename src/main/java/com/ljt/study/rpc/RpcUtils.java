package com.ljt.study.rpc;

import com.ljt.study.rpc.protocol.CustomHeader;
import com.ljt.study.rpc.protocol.CustomRequestBody;
import com.ljt.study.rpc.protocol.CustomResponseBody;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.UUID;

import static com.ljt.study.Constant.LOCAL_HOST;

/**
 * @author LiJingTang
 * @date 2021-03-06 16:45
 */
@Slf4j
public class RpcUtils {

    public static final InetSocketAddress ADDRESS = new InetSocketAddress(LOCAL_HOST, 9090);

    private RpcUtils() {
    }

    private static final ByteArrayOutputStream BYTE_OUT = new ByteArrayOutputStream();

    public static synchronized byte[] serial(Object obj) {
        BYTE_OUT.reset();
        byte[] bytes = null;
        try {
            new ObjectOutputStream(BYTE_OUT).writeObject(obj);
            bytes = BYTE_OUT.toByteArray();
        } catch (IOException e) {
            log.error("序列化异常", e);
        }

        return bytes;
    }

    private static Object unSerial(byte[] bytes) {
        try {
            ObjectInputStream objInput = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return objInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("反序列化异常", e);
            return null;
        }
    }

    public static CustomHeader unSerialHeader(byte[] bytes) {
        return (CustomHeader) unSerial(bytes);
    }

    public static CustomRequestBody unSerialRequestBody(byte[] bytes) {
        return (CustomRequestBody) unSerial(bytes);
    }

    public static CustomResponseBody unSerialResponseBody(byte[] bytes) {
        return (CustomResponseBody) unSerial(bytes);
    }

    public static CustomHeader createHeader(int contentLength) {
        CustomHeader header = new CustomHeader();
        header.setRequest(true);
        UUID uuid = UUID.randomUUID();
        header.setRequestId(Math.abs(uuid.getLeastSignificantBits()));
        header.setContentLength(contentLength);
        return header;
    }

    public static CustomRequestBody createRequestBody(Class<?> clazz, Method method, Object[] args) {
        CustomRequestBody body = new CustomRequestBody();
        body.setTypeName(clazz.getName());
        body.setMethodName(method.getName());
        body.setParameterTypes(method.getParameterTypes());
        body.setArgs(args);
        return body;
    }

}
