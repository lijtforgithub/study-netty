package com.ljt.study.rpc;

import com.ljt.study.rpc.protocol.CustomHeader;
import com.ljt.study.rpc.protocol.RequestBody;
import com.ljt.study.rpc.protocol.ResponseBody;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author LiJingTang
 * @date 2021-03-06 16:45
 */
@Slf4j
public class RpcUtils {

    public static final String PROTOCOL = "protocol";

    private RpcUtils() {
    }


    public static byte[] serial(Object obj) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteOut)) {
            outputStream.writeObject(obj);
            return byteOut.toByteArray();
        } catch (IOException e) {
            log.error("序列化异常", e);
            throw new RuntimeException(e);
        }
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

    public static RequestBody unSerialRequestBody(byte[] bytes) {
        return (RequestBody) unSerial(bytes);
    }

    public static ResponseBody unSerialResponseBody(byte[] bytes) {
        return (ResponseBody) unSerial(bytes);
    }

    public static CustomHeader createHeader(int contentLength) {
        CustomHeader header = new CustomHeader();
        header.setRequest(true);
        UUID uuid = UUID.randomUUID();
        header.setRequestId(Math.abs(uuid.getLeastSignificantBits()));
        header.setContentLength(contentLength);
        return header;
    }

    public static RequestBody createRequestBody(Class<?> clazz, Method method, Object[] args) {
        RequestBody body = new RequestBody();
        body.setTypeName(clazz.getName());
        body.setMethodName(method.getName());
        body.setParameterTypes(method.getParameterTypes());
        body.setArgs(args);
        return body;
    }

    public static Object invoke(String typeName, String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Class<?> clazz = Class.forName(typeName);
            Object obj = Dispatcher.get(clazz);
            Method method = clazz.getMethod(methodName, parameterTypes);
            return method.invoke(obj, args);
        } catch (Exception e) {
            log.error("反射调用方法异常", e);
            return null;
        }
    }

}
