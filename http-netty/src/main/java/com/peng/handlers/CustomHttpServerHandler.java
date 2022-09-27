package com.peng.handlers;

import com.peng.util.RequestUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class CustomHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(CustomHttpServerHandler.class);

    private final StringBuilder responseData = new StringBuilder();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest msg) throws Exception {

        logger.info("接收到了一次http/https请求：\r\n 协议版本：{} \r\n 请求头：{}\r\n 请求方法：{}\r\n 请求内容：{}\r\n",
                msg.protocolVersion(), msg.headers(),
                msg.method(), msg.content().toString(CharsetUtil.UTF_8));

        //1.如果解析有问题，记录计息异常信息直接返回
        if (!msg.decoderResult().isSuccess()) {
            responseData.append(RequestUtils.evaluateDecoderResult(msg));
            writeTextResponse(channelHandlerContext, msg, responseData);
        }

        // 2.处理 http 100请求
        // HTTP/1.1 协议允许客户端在发送RequestMessage之前，发送100请求，用来判定服务器是否愿意接受客户端发送来的消息主体（基于RequstHeader）
        if (HttpUtil.is100ContinueExpected(msg)) {
            writeContinueResponse(channelHandlerContext);
        }

        // 3.构造http响应
        //3.1 添加httpRequst相关信息
        responseData.append(RequestUtils.formatParams(msg));
        //3.2 添加httpContext相关信息
        responseData.append(RequestUtils.formatBody(msg));
        //3.3 添加httpLastContext相关信息
        responseData.append(RequestUtils.prepareLastResponse(msg));

        writeTextResponse(channelHandlerContext, msg, responseData);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //读取完成的时候写入数据
        ctx.flush();
        logger.info("获取消息完成，刷入信息");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.toString());
        ctx.close();
    }

    private void writeContinueResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);
        ctx.write(response);
    }

    private void writeTextResponse(ChannelHandlerContext ctx, FullHttpRequest request, StringBuilder responseData) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                request.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
                Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.UTF_8));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(httpResponse);

        //如果不需要保持连接，直接写入连接，然后关闭通道
        if (!keepAlive) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
