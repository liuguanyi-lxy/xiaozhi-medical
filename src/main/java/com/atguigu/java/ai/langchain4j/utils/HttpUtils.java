package com.atguigu.java.ai.langchain4j.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求工具类
 * 封装Apache HttpClient，提供GET和POST请求方法
 */
public class HttpUtils {

    /**
     * 默认超时时间（毫秒）
     */
    private static final int DEFAULT_TIMEOUT = 5000;

    /**
     * 发送GET请求
     *
     * @param host   主机地址
     * @param path   请求路径
     * @param method 请求方法
     * @param headers 请求头参数
     * @param querys 查询参数
     * @return CloseableHttpResponse响应对象
     * @throws Exception 异常信息
     */
    public static CloseableHttpResponse doGet(
            String host,
            String path,
            String method,
            Map<String, String> headers,
            Map<String, String> querys) throws Exception {

        /**
         * 构建请求URL
         */
        StringBuilder urlBuilder = new StringBuilder(host);
        if (path != null && !path.isEmpty()) {
            urlBuilder.append(path);
        }

        /**
         * 添加查询参数
         */
        if (querys != null && !querys.isEmpty()) {
            urlBuilder.append("?");
            List<NameValuePair> pairs = new ArrayList<>();
            for (Map.Entry<String, String> entry : querys.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            String queryString = EntityUtils.toString(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
            urlBuilder.append(queryString);
        }

        /**
         * 创建HTTP客户端和GET请求
         */
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(urlBuilder.toString());

        /**
         * 设置请求头
         */
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
        }

        /**
         * 设置请求配置
         */
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .setSocketTimeout(DEFAULT_TIMEOUT)
                .build();
        httpGet.setConfig(requestConfig);

        /**
         * 执行请求并返回响应
         */
        return httpClient.execute(httpGet);
    }

    /**
     * 发送POST请求
     *
     * @param host   主机地址
     * @param path   请求路径
     * @param method 请求方法
     * @param headers 请求头参数
     * @param querys 查询参数
     * @param bodys  请求体参数
     * @return CloseableHttpResponse响应对象
     * @throws Exception 异常信息
     */
    public static CloseableHttpResponse doPost(
            String host,
            String path,
            String method,
            Map<String, String> headers,
            Map<String, String> querys,
            Map<String, String> bodys) throws Exception {

        /**
         * 构建请求URL
         */
        StringBuilder urlBuilder = new StringBuilder(host);
        if (path != null && !path.isEmpty()) {
            urlBuilder.append(path);
        }

        /**
         * 添加查询参数
         */
        if (querys != null && !querys.isEmpty()) {
            urlBuilder.append("?");
            List<NameValuePair> pairs = new ArrayList<>();
            for (Map.Entry<String, String> entry : querys.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            String queryString = EntityUtils.toString(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
            urlBuilder.append(queryString);
        }

        /**
         * 创建HTTP客户端和POST请求
         */
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(urlBuilder.toString());

        /**
         * 设置请求头
         */
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }

        /**
         * 设置请求体
         */
        if (bodys != null && !bodys.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>();
            for (Map.Entry<String, String> entry : bodys.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
        }

        /**
         * 设置请求配置
         */
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .setSocketTimeout(DEFAULT_TIMEOUT)
                .build();
        httpPost.setConfig(requestConfig);

        /**
         * 执行请求并返回响应
         */
        return httpClient.execute(httpPost);
    }

    /**
     * 发送POST请求（JSON格式）
     *
     * @param host   主机地址
     * @param path   请求路径
     * @param headers 请求头参数
     * @param jsonBody JSON格式的请求体
     * @return CloseableHttpResponse响应对象
     * @throws Exception 异常信息
     */
    public static CloseableHttpResponse doPostJson(
            String host,
            String path,
            Map<String, String> headers,
            String jsonBody) throws Exception {

        /**
         * 构建请求URL
         */
        StringBuilder urlBuilder = new StringBuilder(host);
        if (path != null && !path.isEmpty()) {
            urlBuilder.append(path);
        }

        /**
         * 创建HTTP客户端和POST请求
         */
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(urlBuilder.toString());

        /**
         * 设置请求头
         */
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }

        /**
         * 设置请求体
         */
        if (jsonBody != null && !jsonBody.isEmpty()) {
            httpPost.setEntity(new org.apache.http.entity.StringEntity(jsonBody, StandardCharsets.UTF_8));
        }

        /**
         * 设置请求配置
         */
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .setSocketTimeout(DEFAULT_TIMEOUT)
                .build();
        httpPost.setConfig(requestConfig);

        /**
         * 执行请求并返回响应
         */
        return httpClient.execute(httpPost);
    }

    /**
     * 关闭HTTP响应
     *
     * @param response 响应对象
     */
    public static void closeResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭HTTP客户端
     *
     * @param httpClient HTTP客户端
     */
    public static void closeHttpClient(CloseableHttpClient httpClient) {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
