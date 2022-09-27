package com.peng.client;

import com.peng.handlers.CustomHttpClientHandler;
import com.sun.jndi.toolkit.url.Uri;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channel;
import java.util.Scanner;

public class CmdRequst implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CmdRequst.class);

    private final ChannelFuture channelFuture;

    public CmdRequst(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    @Override
    public void run() {
        try {
            URI uri = new URI("/user/id=1");
            String requestContext = "test";
            FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                    uri.getPath(), Unpooled.copiedBuffer(requestContext, CharsetUtil.UTF_8));
            request.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            request.headers().add(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
            channelFuture.channel().writeAndFlush(request);

        } catch (URISyntaxException e) {
            logger.warn("构造requst请求失败：{}", e.getCause().toString());
        }
    }
}
