package com.ljt.study.rpc.transport;

import com.ljt.study.rpc.protocol.RequestBody;
import com.ljt.study.rpc.protocol.ResponseBody;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static com.ljt.study.rpc.RpcUtils.HTTP_URL;

/**
 * @author LiJingTang
 * @date 2021-03-07 14:14
 */
@Slf4j
public class BioHttpTransporter implements Transporter {

    @Override
    public CompletableFuture<Object> transport(RequestBody requestBody) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        Object result = null;
        try {
            URL url = new URL(HTTP_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream output = connection.getOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(output);
            objOut.writeObject(requestBody);

            if (HttpResponseStatus.OK.code() == connection.getResponseCode()) {
                InputStream input = connection.getInputStream();
                ObjectInputStream objIn = new ObjectInputStream(input);
                ResponseBody obj = (ResponseBody) objIn.readObject();
                result = obj.getResult();
            }
        } catch (Exception e) {
            log.error("连接异常", e);
        }

        future.complete(result);
        return future;
    }

}
