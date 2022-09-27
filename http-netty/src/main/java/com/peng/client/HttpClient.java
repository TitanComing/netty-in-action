package com.peng.client;

import com.peng.handlers.CustomHttpClientHandler;
import com.peng.handlers.HttpInitializer;
import com.peng.server.HttpServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    private final String host;
    private final int port;

    public HttpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new HttpInitializer(true, false, null));
            //等待连接成功
            ChannelFuture channelFuture = bootstrap.connect().sync();
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("client start success!");
                    channelFuture.channel().eventLoop().execute(new CmdRequst(channelFuture));
                } else {
                    logger.error("client connect failed!");
                }
            });
            //保持连接不要关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        String host = args.length > 1 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
        new HttpClient(host, port).start();
    }
}
