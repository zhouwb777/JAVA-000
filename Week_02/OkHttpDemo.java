package io.github.kimmking.netty.server;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouwb
 * create 2020-10-28 22:02
 */
public class OkHttpDemo {

    private static final String HTTP_JSON = "application/json; charset=utf-8";
    private static final String HTTP_FORM = "application/x-www-form-urlencoded; charset=utf-8";

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build();

    /**
     * @param url
     * @return
     */
    public static String httpGet(String url) {
        if (url == null || "".equals(url)) {
            System.out.println("url为null!");
            return "";
        }

        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(url).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                System.out.println("http GET 请求成功; [url="+url+"]");
                return response.body().string();
            } else {
                System.out.println("Http GET 请求失败; [errorCode = "+response.code()+" , url="+url+"]");
            }
        } catch (IOException e) {
            throw new RuntimeException("同步http GET 请求失败,url:" + url, e);
        }
        return null;
    }

    public static void main(String[] args) {
        httpGet("http://localhost:8801");
    }

}
