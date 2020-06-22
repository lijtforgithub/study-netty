package com.ljt.study.netty.protocol;

import com.ljt.study.netty.Server;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.RandomAccessFile;

import static com.ljt.study.Constant.SYS_SEP;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author LiJingTang
 * @date 2020-05-11 08:58
 */
public class FileServer {

    public static void main(String[] args) {
        new Server(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(new StringEncoder(UTF_8))
                        .addLast(new LineBasedFrameDecoder(1024))
                        .addLast(new StringDecoder(UTF_8))
                        .addLast(new FileServerHandler());
            }
        }).start();
    }

    @Slf4j
    private static class FileServerHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
            log.info("服务端接收到到消息：{}", msg);
            File file = new File(msg);
            if (file.exists()) {
                if (!file.isFile()) {
                    ctx.writeAndFlush("Not a file : " + file + SYS_SEP);
                    return;
                }
                ctx.write(file + " " + file.length() + SYS_SEP);
                try (RandomAccessFile accessFile = new RandomAccessFile(msg, "r")) {
                    DefaultFileRegion region = new DefaultFileRegion(accessFile.getChannel(), 0, accessFile.length());
                    ctx.write(region);
                    ctx.writeAndFlush(SYS_SEP);
                }
            } else {
                ctx.writeAndFlush("File not found: " + file + SYS_SEP);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
            log.error(StringUtils.EMPTY, e);
            ctx.close();
        }
    }

}
