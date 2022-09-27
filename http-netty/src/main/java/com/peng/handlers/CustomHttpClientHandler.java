package com.peng.handlers;

import com.peng.util.RequestUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private static final Logger logger = LoggerFactory.getLogger(CustomHttpClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接到远程服务器：{}",ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        logger.info("接收到了一次http/https响应：\r\n 协议版本：{} \r\n 请求头：{} \r\n 请求内容：{} \r\n",
                msg.protocolVersion(), msg.headers(),
                msg.content().toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.toString());
        ctx.close();
    }

}
