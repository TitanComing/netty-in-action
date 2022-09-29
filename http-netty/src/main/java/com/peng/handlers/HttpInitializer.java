package com.peng.handlers;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

public class HttpInitializer extends ChannelInitializer<Channel> {

    private final SslContext context;
    private final boolean isSsl;
    private final boolean isClient;


    public HttpInitializer(boolean isClient, boolean isSsl, SslContext context) {
        this.isClient = isClient;
        this.isSsl = isSsl;
        this.context = context;

    }

    @Override
    protected void initChannel(Channel channel) throws Exception {

        ChannelPipeline channelPipeline = channel.pipeline();

        if (isSsl) {
            SSLEngine engine = context.newEngine(channel.alloc());
            //将 SslHandler 添加到ChannelPipeline 中以使用 HTTPS
            channelPipeline.addFirst("ssl", new SslHandler(engine));
        }

        //增加日志文件记录
        channelPipeline.addLast("logger", new LoggingHandler(LogLevel.WARN));

        if (isClient) {
            //字节和HttpRequest、HttpContent和LastHttpContent消息之间的编解码    in , out
            channelPipeline.addLast("codec", new HttpClientCodec());
            //聚合HttpRequest、HttpContent和LastHttpContent消息为FullHttpRequest 最大2M   in
            channelPipeline.addLast("aggregator", new HttpObjectAggregator(2 * 1024 * 1024));
            //如果是客户端，则添加 HttpContentDecompressor 以处理来自服务器的压缩内容    in
            channelPipeline.addLast("decompressor", new HttpContentDecompressor());
            //增加客户端的自定义处理器     in
            channelPipeline.addLast("custHttpClientHandler",new CustomHttpClientHandler());
        } else {
            //如果是服务器
            //字节和HttpRequest、HttpContent和LastHttpContent消息之间的编解码   in out
            channelPipeline.addLast("codec", new HttpServerCodec());
            //聚合HttpRequest、HttpContent和LastHttpContent消息为FullHttpRequest 最大2M   in
            channelPipeline.addLast("aggregator", new HttpObjectAggregator(2 * 1024 * 1024));
            //如果是服务器，则添加HttpContentCompressor 来压缩数据（如果客户端支持它）  in(这里有个匹配器，匹配处理入参还是出参) out
            channelPipeline.addLast("compressor", new HttpContentCompressor());
            //增加自定义的处理器                             in
            channelPipeline.addLast("custHttpServerHandler",new CustomHttpServerHandler());
        }
    }
}
