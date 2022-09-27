package com.peng.server;

import com.peng.handlers.HttpInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 基于netty的httpServer服务器
 */
public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private final int port;

    public HttpServer(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new HttpInitializer(false, false, null));

            ChannelFuture channelFuture = bootstrap.bind();
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("Server init channel pipeline : {}", channelFuture.channel().pipeline().names());
                    logger.info("Server start at {}", port);
                } else {
                    logger.error("Server Bind attempt failed: {}", future.cause().toString());
                }
            });
            channelFuture.sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new HttpServer(port).run();
    }
}
