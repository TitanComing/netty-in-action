package com.peng.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ProxyInitializer extends ChannelInitializer<Channel> {

    private final String remoteHost;
    private final int remotePort;

    public ProxyInitializer(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO))
                .addLast(new ProxyServerFrontendHandler(remoteHost, remotePort));
    }
}
