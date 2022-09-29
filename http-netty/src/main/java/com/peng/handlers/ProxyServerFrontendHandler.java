package com.peng.handlers;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SerializationUtils;

/**
 * 代理服务器的前端处理
 */
public class ProxyServerFrontendHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ProxyServerFrontendHandler.class);
    private final String remoteHost;
    private final int remotePort;

    //出站数据，用于实际的服务
    private Channel outboundChannel;

    public ProxyServerFrontendHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    //客户端连接到代理服务器的时候，代理服务器发起到后台服务的连接
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Channel inboundChannel = ctx.channel();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new ProxyServerBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture channelFuture = bootstrap.connect(remoteHost, remotePort);
        outboundChannel = channelFuture.channel();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    //连接后台服务成功的时候，手动触发read()操作
                    inboundChannel.read();
                } else {
                    //连接后台服务失败的时候，关闭入站连接
                    inboundChannel.close();
                }
            }
        });
    }

    //重写了channelRead()方法，在上面调用
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // was able to flush out data, start to read the next chunk
                    // 发送成功，然后写入下一个数据包
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.toString());
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
