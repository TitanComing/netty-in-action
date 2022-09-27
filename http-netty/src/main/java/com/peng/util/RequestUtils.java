package com.peng.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;

/**
 * http request处理工具类
 */
public class RequestUtils {

    //解析http请求参数
    public static StringBuilder formatParams(HttpRequest request) {
        StringBuilder requestParameters = new StringBuilder();
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        if (!params.isEmpty()) {
            requestParameters.append("请求参数:\r\n");
            for (Map.Entry<String, List<String>> p : params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    requestParameters.append(key.toUpperCase())
                            .append(" = ")
                            .append(val.toUpperCase())
                            .append("\r\n");
                }
            }
        }
        return requestParameters;
    }


    //解析http请求content
    public static StringBuilder formatBody(HttpContent httpContent) {
        StringBuilder requestContent = new StringBuilder();
        requestContent.append("请求内容:\r\n");
        ByteBuf content = httpContent.content();
        if (content.isReadable()) {
            requestContent.append(content.toString(CharsetUtil.UTF_8).toUpperCase());
        }
        return requestContent;
    }


    //准备LastResponse
    public static StringBuilder prepareLastResponse(LastHttpContent trailer) {
        StringBuilder responseData = new StringBuilder();
        responseData.append("请求尾部：\r\n");
        if (!trailer.trailingHeaders().isEmpty()) {
            for (CharSequence name : trailer.trailingHeaders().names()) {
                for (CharSequence value : trailer.trailingHeaders().getAll(name)) {
                    responseData.append("Trailing Header: \r\n");
                    responseData.append(name)
                            .append(" = ")
                            .append(value)
                            .append("\r\n");
                }
            }
        }
        responseData.append("Good Bye!\r\n");
        return responseData;
    }

    //异常解析处理
    public static StringBuilder evaluateDecoderResult(HttpObject o) {
        StringBuilder responseData = new StringBuilder();
        DecoderResult result = o.decoderResult();

        if (!result.isSuccess()) {
            responseData.append("request error: ");
            responseData.append(result.cause());
            responseData.append("\r\n");
        }

        return responseData;
    }

}
