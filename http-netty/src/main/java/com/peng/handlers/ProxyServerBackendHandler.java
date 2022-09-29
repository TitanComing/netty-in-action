package com.peng.handlers;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServerBackendHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ProxyServerBackendHandler.class);
    private final Channel inboundChannel;

    public ProxyServerBackendHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //读取到入站信息（对后台真正的服务来说，是客户端，所以入站信息是后台服务的返回），然后写给前置连接
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    ctx.channel().read();
                } else {
                    channelFuture.channel().close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ProxyServerFrontendHandler.closeOnFlush(ctx.channel());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.toString());
        ProxyServerFrontendHandler.closeOnFlush(ctx.channel());
    }
}
