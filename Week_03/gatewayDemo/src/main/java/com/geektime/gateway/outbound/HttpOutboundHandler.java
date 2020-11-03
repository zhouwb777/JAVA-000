package com.geektime.gateway.outbound;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpOutboundHandler {
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
    private ExecutorService proxyService;
    private String backendUrl;
    
    public HttpOutboundHandler(String backendUrl){
        this.backendUrl = backendUrl.endsWith("/")?backendUrl.substring(0,backendUrl.length()-1):backendUrl;
        int cores = Runtime.getRuntime().availableProcessors() * 2;
        long keepAliveTime = 1000;
        int queueSize = 2048;
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();//.DiscardPolicy();
        proxyService = new ThreadPoolExecutor(cores, cores,
                keepAliveTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(queueSize),
                new com.geektime.gateway.outbound.okhttp.NamedThreadFactory("proxyService"), handler);
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        final String url = this.backendUrl + fullRequest.uri();
        proxyService.submit(()->httpGet(fullRequest, ctx, url));
    }

    /**
     * get请求
     */
    public void httpGet(final FullHttpRequest inbound, final ChannelHandlerContext ctx, final String url) {
        final Request.Builder builder = new Request.Builder().get().url(url);
        //添加headFilter的header信息
        for (Map.Entry<String, String> header : inbound.headers()) {
            builder.addHeader(header.getKey(),header.getValue());
        }
        builder.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
        Request request = builder.build();

        FullHttpResponse response = null;
        try {
            Response result = okHttpClient.newCall(request).execute();
            String responeBody;
            if (result.code() < 200 ||result.code()>=300) {
                responeBody = "request error";
            }else{
                responeBody = Objects.requireNonNull(result.body()).string();
            }
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responeBody.getBytes()));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", responeBody.length());
            System.out.println("request url:" + url + " , response is " + response.content().toString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (!HttpUtil.isKeepAlive(inbound)) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.write(response);
            }
            ctx.flush();
        }
    }


    private void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


    
}
