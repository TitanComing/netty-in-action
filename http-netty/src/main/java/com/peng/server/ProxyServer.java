package com.peng.server;

import com.peng.handlers.ProxyInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 基于netty的httpServer服务器
 */
public class ProxyServer {
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);
    private final int localPort;
    private static final String remoteHost = "127.0.0.1";
    private static final int remotePort = 8080;

    public ProxyServer(int localPort) {
        this.localPort = localPort;
    }

    public void run() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(localPort))
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ProxyInitializer(remoteHost, remotePort))
                    // 设置netty数据到来的时候不自动调用 channel.read()方法，从而手动调用
                    .childOption(ChannelOption.AUTO_READ, false);

            ChannelFuture channelFuture = bootstrap.bind();
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.info("Server init channel pipeline : {}", channelFuture.channel().pipeline().names());
                    logger.info("Server start at {}", localPort);
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
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8090;
        new ProxyServer(port).run();
    }
}
