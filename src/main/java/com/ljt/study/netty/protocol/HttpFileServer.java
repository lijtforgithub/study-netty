package com.ljt.study.netty.protocol;

import com.ljt.study.netty.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.LastHttpContent.EMPTY_LAST_CONTENT;
import static io.netty.util.CharsetUtil.ISO_8859_1;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author LiJingTang
 * @date 2020-05-09 09:29
 */
@Slf4j
public class HttpFileServer {

    private static final String DEF_URL = "/study-netty";
    private static final String DIR = System.getProperty("user.dir");

    public static void main(String[] args) {
        log.info(DIR);
        new Server(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast("http-decoder", new HttpRequestDecoder())
                        .addLast("http-aggregator", new HttpObjectAggregator(65536))
                        .addLast("http-encoder", new HttpResponseEncoder())
                        .addLast("http-chunked", new ChunkedWriteHandler())
                        .addLast("fileServerHandler", new HttpFileServerHandler(DEF_URL));
            }
        }).start();
    }

    @Slf4j
    private static class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private static final char PT = '.';
        private static final char SEP = '/';
        private static final String ICO = "favicon.ico";
        private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*]");
        private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

        private final String url;

        public HttpFileServerHandler(String url) {
            this.url = url;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            if (!request.getDecoderResult().isSuccess()) {
                sendStatus(ctx, BAD_REQUEST);
                return;
            }
            if (request.getMethod() != GET) {
                sendStatus(ctx, METHOD_NOT_ALLOWED);
                return;
            }

            final String uri = request.getUri();
            final String path = sanitizeUri(uri);
            log.info("{} -> {}", uri, path);
            if (uri.endsWith(ICO)) {
                sendStatus(ctx, NOT_FOUND);
                return;
            }
            if (StringUtils.isBlank(path)) {
                sendStatus(ctx, FORBIDDEN);
                return;
            }

            File file = new File(path);
            if (file.isHidden() || !file.exists()) {
                sendStatus(ctx, NOT_FOUND);
                return;
            }
            if (file.isDirectory()) {
                if (uri.endsWith(String.valueOf(SEP))) {
                    sendListing(ctx, file);
                } else {
                    sendRedirect(ctx, uri + SEP);
                }
                return;
            }
            if (!file.isFile()) {
                sendStatus(ctx, FORBIDDEN);
                return;
            }

            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                long fileLength = randomAccessFile.length();
                HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
                setContentLength(response, fileLength);
                setContentTypeHeader(response, file);
                if (isKeepAlive(request)) {
                    response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                }
                ctx.write(response);

                ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
                sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                    @Override
                    public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                        if (total < 0) {
                            log.info("Transfer progress: {}", progress);
                        } else {
                            log.info("Transfer progress: {} / {}", progress, total);
                        }
                    }

                    @Override
                    public void operationComplete(ChannelProgressiveFuture future) {
                        log.info("Transfer complete.");
                    }
                });

                ChannelFuture future = ctx.writeAndFlush(EMPTY_LAST_CONTENT);
                if (!isKeepAlive(request)) {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
            } catch (FileNotFoundException e) {
                sendStatus(ctx, NOT_FOUND);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
            log.error(StringUtils.EMPTY, e);
            if (ctx.channel().isActive()) {
                sendStatus(ctx, INTERNAL_SERVER_ERROR);
            }
        }


        private void setContentTypeHeader(HttpResponse response, File file) {
            MimetypesFileTypeMap map = new MimetypesFileTypeMap();
            response.headers().set(CONTENT_TYPE, map.getContentType(file.getPath()));
        }

        private void sendRedirect(ChannelHandlerContext ctx, String s) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
            response.headers().set(LOCATION, s);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        private void sendListing(ChannelHandlerContext ctx, File file) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
            response.headers().set(CONTENT_TYPE, "text/html;charset=UTF-8");
            StringBuilder strBuilder = new StringBuilder();
            String path = file.getPath();
            strBuilder.append("<!DOCTYPE html>\r\n")
                    .append("<html><head><title>")
                    .append(path)
                    .append(" 目录： ")
                    .append("</title></head><body>\r\n")
                    .append("<h3>")
                    .append(path).append(" 目录： ")
                    .append("</h3>\r\n")
                    .append("<ul>")
                    .append("<li>链接：<a href=\"../\">..</a></li>\r\n");
            File[] files = file.listFiles();
            if (Objects.nonNull(files)) {
                Stream.of(files)
                        .filter(f -> !f.isHidden() && f.canRead() && ALLOWED_FILE_NAME.matcher(f.getName()).matches())
                        .forEach(f -> strBuilder.append("<li>链接：<a href=\"")
                                .append(f.getName())
                                .append("\">")
                                .append(f.getName())
                                .append("</a></li>\r\n"));
            }
            strBuilder.append("</ul></body></html>\r\n");
            ByteBuf byteBuf = Unpooled.copiedBuffer(strBuilder, UTF_8);
            response.content().writeBytes(byteBuf);
            byteBuf.release();
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        private String sanitizeUri(String uri) {
            try {
                uri = URLDecoder.decode(uri, UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                try {
                    uri = URLDecoder.decode(uri, ISO_8859_1.name());
                } catch (UnsupportedEncodingException e1) {
                    log.error(StringUtils.EMPTY, e1);
                }
            }

            if (!uri.startsWith(url) || !uri.startsWith(String.valueOf(SEP))) {
                return null;
            }
            uri = uri.replace(SEP, File.separatorChar);
            if (uri.contains(File.separator + PT) || uri.contains(PT + File.separator)
                    || uri.endsWith(String.valueOf(PT)) || INSECURE_URI.matcher(uri).matches()) {
                return null;
            }

            return DIR + File.separator + uri;
        }

        private void sendStatus(ChannelHandlerContext ctx, HttpResponseStatus status) {
            log.warn(status.toString());
            ByteBuf byteBuf = Unpooled.copiedBuffer(status.toString(), UTF_8);
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, byteBuf);
            response.headers().set(CONTENT_TYPE, "text/plain;charset=UTF-8");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
