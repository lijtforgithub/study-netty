package com.ljt.study.rpc.transport;

import com.ljt.study.rpc.protocol.RequestBody;
import com.ljt.study.rpc.protocol.ResponseBody;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * @author LiJingTang
 * @date 2021-03-07 14:14
 */
@Slf4j
public class BioHttpTransporter implements Transporter {

    @Override
    public CompletableFuture<Object> transport(String host, int port, RequestBody requestBody) throws Exception {
        CompletableFuture<Object> future = new CompletableFuture<>();
        Object result = null;

        URL url = new URL(String.format("http://%s:%s/", host, port));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(HttpMethod.POST.name());
        connection.setDoOutput(true);

        ObjectOutputStream objOut = new ObjectOutputStream(connection.getOutputStream());
        objOut.writeObject(requestBody);

        if (HttpResponseStatus.OK.code() == connection.getResponseCode()) {
            ObjectInputStream objIn = new ObjectInputStream(connection.getInputStream());
            ResponseBody obj = (ResponseBody) objIn.readObject();
            result = obj.getResult();
        }

        future.complete(result);
        return future;
    }

}
