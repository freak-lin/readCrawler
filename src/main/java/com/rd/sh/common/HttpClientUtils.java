package com.rd.sh.common;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mozilla.universalchardet.UniversalDetector;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.mozilla.universalchardet.UniversalDetector;

public class HttpClientUtils {
    private static int statusCode = 200;
    private static final Pattern charsetPattern = Pattern
            .compile("(?i)\\bcharset=\\s*(?:\"|')?([^\\s,;\"']*)");

    /**
     * 发送post请求
     * @param url
     * @param params
     * @return
     */
    public static String post(String url, String params, String headers) {
        String content = "";
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(10 * 1000).setConnectTimeout(10 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .setStaleConnectionCheckEnabled(true).build();
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();
        // 创建httppost
        HttpPost httppost = new HttpPost(url);
        httppost.setConfig(defaultRequestConfig);
        // 创建参数队列
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        String[] splitParams = params.split("&");
        if(!(splitParams.length==1&&splitParams[0].equals(""))) {
            for(int i = 0; i < splitParams.length; i++) {
                String[] pair = splitParams[i].split("=", 2);
                formparams.add(new BasicNameValuePair(pair[0], pair[1]));
            }
        }
        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formparams);
            httppost.setEntity(uefEntity);
            String[] splitHeaders = headers.split("&");
            if(!(splitHeaders.length==1&&splitHeaders[0].equals(""))) {
                for(int i = 0; i < splitHeaders.length; i++) {
                    String[] pair = splitHeaders[i].split("=", 2);
                    httppost.addHeader(pair[0], pair[1]);
                }
            }
            System.out.print("executing request " + httppost.getURI());
            CloseableHttpResponse response = httpclient.execute(httppost);
            System.out.println("\t"+response.getStatusLine());
            HttpEntity entity = response.getEntity();
            if(entity != null) {
                content = EntityUtils.toString(entity, "UTF-8");
            }
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch(ClientProtocolException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }
    /**
     * 发送带有header的get请求
     * （格式为key=value&.....模式）
     * @param url
     * @param headers
     * @return
     */
    public static String sendGet(String url, String headers) {
        {
            String htmlString = "";
            // CloseableHttpClient httpclient = HttpClients.createDefault();
            // 客户请求超时配置
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(30000).setConnectTimeout(30000)
                    .setConnectionRequestTimeout(30000)
                    .setStaleConnectionCheckEnabled(true).build();

            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultRequestConfig(defaultRequestConfig).build();

            try {
                // 创建httpget.
                HttpGet httpget = new HttpGet(url);
                // 复制客户端超时设置
                RequestConfig requestConfig = RequestConfig.copy(
                        defaultRequestConfig).build();
                httpget.setConfig(requestConfig);
                String[] splitHeaders = headers.split("&");
                if(!(splitHeaders.length==1&&splitHeaders[0].equals(""))) {
                    for(int i = 0; i < splitHeaders.length; i++) {
                        String[] pair = splitHeaders[i].split("=", 2);
                        httpget.addHeader(pair[0], pair[1]);
                    }
                }
                System.out.println("--------------------------------------");
                System.out.print("executing request " + httpget.getURI());
                // 执行get请求.
                CloseableHttpResponse response = httpclient.execute(httpget);
                try {
                    // 获取响应实体
                    HttpEntity entity = response.getEntity();
//					System.out.println("--------------------------------------");
                    // 打印响应状态
                    System.out.print("\t"+response.getStatusLine());
                    statusCode = response.getStatusLine().getStatusCode();
                    if (entity != null) {
                        // 打印响应内容长度
                        System.out.print("\tResponse content length: "
                                + entity.getContentLength());

                        // charset处理
                        String charset = getContentCharSet(entity);
                        if (charset != null && charset.length() > 0) {
                            if (charset.equalsIgnoreCase("gb2312")) {
                                charset = "GBK";
                            }
                            htmlString = EntityUtils.toString(entity, charset);
                        } else {
                            byte[] bytes = EntityUtils.toByteArray(entity);
                            String docData = new String(bytes, "UTF-8");
                            charset = getcharsetbydoc(docData);
                            if (charset.equalsIgnoreCase("gb2312")) {
                                charset = "GBK";
                            }
                            htmlString = new String(bytes, charset);
                        }
                        System.out.print("\tcharset:" + charset);
                    }
//					System.out.println("------------------------------------");
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 关闭连接,释放资源
                try {
                    httpclient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\texecuted");
            return htmlString;
        }

    }

    /**
     * HttpClient连接SSL
     */
    public void ssl() {
        CloseableHttpClient httpclient = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            FileInputStream instream = new FileInputStream(new File(
                    "d:\\tomcat.keystore"));
            try {
                // 加载keyStore d:\\tomcat.keystore
                trustStore.load(instream, "123456".toCharArray());
            } catch (CertificateException e) {
                e.printStackTrace();
            } finally {
                try {
                    instream.close();
                } catch (Exception ignore) {
                }
            }
            // 相信自己的CA和所有自签名的证书
            SSLContext sslcontext = SSLContexts
                    .custom()
                    .loadTrustMaterial(trustStore,
                            new TrustSelfSignedStrategy()).build();
            // 只允许使用TLSv1协议
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[] { "TLSv1" },
                    null,
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            httpclient = HttpClients.custom().setSSLSocketFactory(sslsf)
                    .build();
            // 创建http请求(get方式)
            HttpGet httpget = new HttpGet(
                    "https://localhost:8443/myDemo/Ajax/serivceJ.action");
            System.out.println("executing request" + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                HttpEntity entity = response.getEntity();
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    System.out.println("Response content length: "
                            + entity.getContentLength());
                    System.out.println(EntityUtils.toString(entity));
                    EntityUtils.consume(entity);
                }
            } finally {
                response.close();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } finally {
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * post方式提交表单（模拟用户登录请求）
     */
    // public void postForm() {
    // // 创建默认的httpClient实例.
    // CloseableHttpClient httpclient = HttpClients.createDefault();
    // // 创建httppost
    // HttpPost httppost = new
    // HttpPost("http://localhost:8080/myDemo/Ajax/serivceJ.action");
    // // 创建参数队列
    // List<namevaluepair> formparams = new ArrayList<namevaluepair>();
    // formparams.add(new BasicNameValuePair("username", "admin"));
    // formparams.add(new BasicNameValuePair("password", "123456"));
    // UrlEncodedFormEntity uefEntity;
    // try {
    // uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
    // httppost.setEntity(uefEntity);
    // System.out.println("executing request " + httppost.getURI());
    // CloseableHttpResponse response = httpclient.execute(httppost);
    // try {
    // HttpEntity entity = response.getEntity();
    // if (entity != null) {
    // System.out.println("--------------------------------------");
    // System.out.println("Response content: " + EntityUtils.toString(entity,
    // "UTF-8"));
    // System.out.println("--------------------------------------");
    // }
    // } finally {
    // response.close();
    // }
    // } catch (ClientProtocolException e) {
    // e.printStackTrace();
    // } catch (UnsupportedEncodingException e1) {
    // e1.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // } finally {
    // // 关闭连接,释放资源
    // try {
    // httpclient.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    // }

    /**
     * 发送 post请求访问本地应用并根据传递参数不同返回不同结果
     */
    @SuppressWarnings("finally")
    public String requestPost(String url, String jsonstr) {
        String content = "";
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(60 * 1000).setConnectTimeout(60 * 1000)
                .setConnectionRequestTimeout(60 * 1000)
                .setStaleConnectionCheckEnabled(true).build();
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost(url);
        // 复制客户端超时设置
        RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig)
                .build();
        httppost.setConfig(requestConfig);
        // 创建参数队列
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("jstr", jsonstr));
        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(uefEntity);
            System.out.println("executing request " + httppost.getURI());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.err.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out
                            .println("--------------------------------------");
                    content = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("Response content: " + content);
                    System.out
                            .println("--------------------------------------");
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            content = "{\"status\":\"1\"}";
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }
    }

    /**
     * 发送 post请求访问本地应用并根据传递参数不同返回不同结果
     */
    @SuppressWarnings("finally")
    public static String requestPost(String url, HashMap<String,String> _paramsmap) {
        String content = "";
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(10 * 1000).setConnectTimeout(10 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .setStaleConnectionCheckEnabled(true).build();
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost(url);
        // 复制客户端超时设置
        RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig)
                .build();
        httppost.setConfig(requestConfig);
        // 创建参数队列
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for(Entry<String, String> paramsentry:_paramsmap.entrySet()){
            formparams.add(new BasicNameValuePair(paramsentry.getKey(), paramsentry.getValue()));
        }

        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httppost.setEntity(uefEntity);
            System.out.println("executing request " + httppost.getURI());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.err.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out
                            .println("--------------------------------------");
                    content = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("Response content: " + content);
                    System.out
                            .println("--------------------------------------");
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }
    }


    /**
     * 发送 get请求
     */
    public static String get4DectEncode(String url) {
        String htmlString = "";
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(30000).setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .setStaleConnectionCheckEnabled(true).build();

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();

        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 复制客户端超时设置
            RequestConfig requestConfig = RequestConfig.copy(
                    defaultRequestConfig).build();
            httpget.setConfig(requestConfig);
            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                System.out.println("--------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                statusCode = response.getStatusLine().getStatusCode();
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: "
                            + entity.getContentLength());

                    // charset处理
                    byte[] bytes=EntityUtils.toByteArray(entity);
                    String contentEncode= detect(bytes);
                    System.out.println("contentEncode:" + contentEncode);
                    String charset = getContentCharSet(entity);
                    if (charset != null && charset.length() > 0) {
                        System.out.println("---------------"+charset);
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        if(contentEncode.equalsIgnoreCase(charset)||contentEncode.substring(0, 2).equalsIgnoreCase(charset.substring(0, 2))){
                            System.out.println("charset case1:" + charset);
                            htmlString = EntityUtils.toString(entity, charset);
                        }

                    } else {
//						byte[] bytes = EntityUtils.toByteArray(entity1);
                        String docData = new String(bytes, "UTF-8");
                        charset = getcharsetbydoc(docData);
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        if(contentEncode.equalsIgnoreCase(charset)||contentEncode.substring(0, 2).equalsIgnoreCase(charset.substring(0, 2))){
                            System.out.println("charset case2:" + charset);
                            htmlString = new String(bytes, charset);
                        }
                        else if(contentEncode.substring(0, 2).equalsIgnoreCase("gb")){
                            charset = "GBK";
                            htmlString = new String(bytes, charset);
                        }
                        else if(contentEncode.equalsIgnoreCase("utf-8")){
                            charset = "utf-8";
                            htmlString = new String(bytes, charset);
                        }
                    }

                }
                System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlString;
    }


    public static String getByproxy(String url,String pro,String prot,String[] headers) throws IOException {
        String htmlString = "";
        //设置代理IP、端口、协议（请分别替换）
        HttpHost proxy = new HttpHost(pro, Integer.parseInt(prot), "http");

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoKeepAlive(false)
                .setSoLinger(1)
                .setSoReuseAddress(true)
                .setSoTimeout(30000)
                .setTcpNoDelay(true).build();

        //把代理设置到请求配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setProxy(proxy).setSocketTimeout(30000).setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .setStaleConnectionCheckEnabled(true)
                .build();
        //实例化CloseableHttpClient对象
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultSocketConfig(socketConfig).setDefaultRequestConfig(defaultRequestConfig).build();
        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 复制客户端超时设置
            RequestConfig requestConfig = RequestConfig.copy(
                    defaultRequestConfig).build();
            httpget.setConfig(requestConfig);
            //设置请求头
            httpget = setHeader(httpget,headers);
            System.out.println("executing request " + httpget.getURI());
            CloseableHttpResponse response=null;
            try {
                // 执行get请求.
                response = httpclient.execute(httpget);
                System.out.println("status:"+response.getStatusLine());
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                System.out.println("--------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                statusCode = response.getStatusLine().getStatusCode();
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: "
                            + entity.getContentLength());

                    // charset处理
                    String charset = getContentCharSet(entity);
                    if (charset != null && charset.length() > 0) {
                        System.out.println("---------------"+charset);
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        htmlString = EntityUtils.toString(entity, charset);
                    } else {
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        String docData = new String(bytes, "UTF-8");
                        charset = getcharsetbydoc(docData);
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        htmlString = new String(bytes, charset);
                    }
                    System.out.println("charset:" + charset);
                }
                System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return htmlString;
    }
    public static HttpGet setHeader(HttpGet get, String[] headers) { ;
        if (headers.length == 0) {
            return get;
        }
        for (String header : headers) {
            if(header.equals("")){
                continue;
            }

            String[] split = header.split(":", 2);
            get.setHeader(split[0], split[1]);

        }
        return get;
    }
    /**
     * 发送 get请求
     */
    public static String get(String url) {
        String htmlString = "";
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(30000).setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .setStaleConnectionCheckEnabled(true).build();

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();

        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 复制客户端超时设置
            RequestConfig requestConfig = RequestConfig.copy(
                    defaultRequestConfig).build();
            httpget.setConfig(requestConfig);
            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                System.out.println("--------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                statusCode = response.getStatusLine().getStatusCode();
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: "
                            + entity.getContentLength());

                    // charset处理
                    String charset = getContentCharSet(entity);
                    if (charset != null && charset.length() > 0) {
                        System.out.println("---------------"+charset);
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        htmlString = EntityUtils.toString(entity, charset);
                    } else {
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        String docData = new String(bytes, "UTF-8");
                        charset = getcharsetbydoc(docData);
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        htmlString = new String(bytes, charset);
                    }
                    System.out.println("charset:" + charset);
                }
                System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlString;
    }

    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
    /**
     * 发送 get请求
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String get4https(String inputUrl) throws Exception {
        trustEveryone() ;
        boolean checkstatus=false;
        HttpURLConnection httpUrlConnection = null;
        System.out.println("executing request " + inputUrl);
        URL url = new URL(inputUrl);
        URLConnection urlConnection = url.openConnection();
        httpUrlConnection = (HttpURLConnection) urlConnection;
        //禁止此次访问重定向
        httpUrlConnection.setInstanceFollowRedirects(false);
        //设置连接超时
        httpUrlConnection.setConnectTimeout(5000);
        httpUrlConnection.setReadTimeout(5000);
        httpUrlConnection.connect();
        InputStream in = httpUrlConnection.getInputStream();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            output.write(buffer, 0, n);
        }
        //     String tmp=new String(output.toByteArray());
        String encode= detect(output.toByteArray()) ;
        System.out.println("------encode---------:"+encode);
        String htmlString = new String(output.toByteArray(), encode);
        return  htmlString;
    }


    public String getV2(String url) {
        String htmlString = "";
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(30000).setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .setStaleConnectionCheckEnabled(true).build();

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();

        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 复制客户端超时设置
            RequestConfig requestConfig = RequestConfig.copy(
                    defaultRequestConfig).build();
            httpget.setConfig(requestConfig);
            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                System.out.println("--------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                statusCode = response.getStatusLine().getStatusCode();
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: "
                            + entity.getContentLength());

                    // charset处理
//					String charset = getContentCharSet(entity);
//					if (charset != null && charset.length() > 0) {
//						if (charset.equalsIgnoreCase("gb2312")) {
//							charset = "GBK";
//						}
//						htmlString = EntityUtils.toString(entity, charset);
//					} else {
                    byte[] bytes = EntityUtils.toByteArray(entity);
                    String charset=detect(bytes);
//						String docData = new String(bytes, "UTF-8");
//						charset = getcharsetbydoc(docData);
//						if (charset.equalsIgnoreCase("gb2312")) {
//							charset = "GBK";
//						}
                    if(charset!=null&&!charset.isEmpty()){
                        htmlString = new String(bytes, charset);
                    }else{
                        htmlString = new String(bytes, "GBK");
                    }

//					}
                    System.out.println("charset:" + charset);
                }
                System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlString;
    }
    public static String get(String url, String charset) {
        String htmlString = "";
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(30000).setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .setStaleConnectionCheckEnabled(true).build();

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();

        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 复制客户端超时设置
            RequestConfig requestConfig = RequestConfig.copy(
                    defaultRequestConfig).build();
            httpget.setConfig(requestConfig);
            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                System.out.println("--------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                statusCode = response.getStatusLine().getStatusCode();
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: "
                            + entity.getContentLength());
                    // charset处理
                    System.out.println("charset:" + charset);
                    byte[] bytes = EntityUtils.toByteArray(entity);
                    htmlString = new String(bytes, charset);
                }
                System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlString;
    }
    /**
     * 发送 get请求获取非乱码的字符串
     */
//	public static String getV2(String url) {
//		String htmlString = "";
//		// CloseableHttpClient httpclient = HttpClients.createDefault();
//		// 客户请求超时配置
//		RequestConfig defaultRequestConfig = RequestConfig.custom()
//				.setSocketTimeout(30000).setConnectTimeout(30000)
//				.setConnectionRequestTimeout(30000)
//				.setStaleConnectionCheckEnabled(true).build();
//
//		CloseableHttpClient httpclient = HttpClients.custom()
//				.setDefaultRequestConfig(defaultRequestConfig).build();
//
//		try {
//			// 创建httpget.
//			HttpGet httpget = new HttpGet(url);
//			// 复制客户端超时设置
//			RequestConfig requestConfig = RequestConfig.copy(
//					defaultRequestConfig).build();
//			httpget.setConfig(requestConfig);
//			System.out.println("executing request " + httpget.getURI());
//			// 执行get请求.
//			CloseableHttpResponse response = httpclient.execute(httpget);
//			try {
//				// 获取响应实体
//				HttpEntity entity = response.getEntity();
//				System.out.println("--------------------------------------");
//				// 打印响应状态
//				System.out.println(response.getStatusLine());
//				statusCode = response.getStatusLine().getStatusCode();
//				if (entity != null) {
//					// 打印响应内容长度
//					System.out.println("Response content length: "
//							+ entity.getContentLength());
//
//					// charset处理
//					String charset = getContentCharSet(entity);
//					if (charset != null && charset.length() > 0) {
//						if (charset.equalsIgnoreCase("gb2312")) {
//							charset = "GBK";
//						}
//						htmlString = EntityUtils.toString(entity, charset);
//					} else {
//						byte[] bytes = EntityUtils.toByteArray(entity);
//						String docData = new String(bytes, "UTF-8");
//						charset = getcharsetbydoc(docData);
//						if (charset.equalsIgnoreCase("gb2312")) {
//							charset = "GBK";
//						}
//						String charsetcode=null;
//						try {
//							charsetcode = detect(bytes);
//							htmlString = new String(bytes, charsetcode);
//						} catch (Exception e) {
//
//							htmlString = new String(bytes, charset);
//						}
//
//					}
//					System.out.println("charset:" + charset);
//				}
//				System.out.println("------------------------------------");
//			} finally {
//				response.close();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			// 关闭连接,释放资源
//			try {
//				httpclient.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return htmlString;
//	}

    /**
     * 下载图片
     * */
    public String downloadimgs(String url, String filename) {
        String htmlString = "";
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(3000).setConnectTimeout(3000)
                .setConnectionRequestTimeout(3000)
                .setStaleConnectionCheckEnabled(true).build();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();
        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 复制客户端超时设置
            RequestConfig requestConfig = RequestConfig.copy(
                    defaultRequestConfig).build();
            httpget.setConfig(requestConfig);
            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                System.out.println("--------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: "
                            + entity.getContentLength());
                    long a = System.currentTimeMillis();
                    InputStream input = entity.getContent();
                    OutputStream output = new FileOutputStream(new File(
                            filename));
                    IOUtils.copy(input, output);
                    output.flush();
                    output.close();
                    input.close();
                    long b = System.currentTimeMillis();
                    System.err.println(b - a);
                }
                System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlString;
    }



    /**
     * 判断图片类型
     * */
    public String getimgtype(String url) {
        String filename = "";
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(3000).setConnectTimeout(3000)
                .setConnectionRequestTimeout(3000)
                .setStaleConnectionCheckEnabled(true).build();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();
        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 复制客户端超时设置
            RequestConfig requestConfig = RequestConfig.copy(
                    defaultRequestConfig).build();
            httpget.setConfig(requestConfig);
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                // 打印响应状态
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    //获取header Content-Type
                    filename = entity.getContentType().getValue();
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filename;
    }




    /**
     * 上传文件
     */
    public void upload() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(
                    "http://localhost:8080/myDemo/Ajax/serivceFile.action");

            FileBody bin = new FileBody(new File("F:\\image\\sendpix0.jpg"));
            StringBody comment = new StringBody("A binary file of some kind",
                    ContentType.TEXT_PLAIN);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("bin", bin).addPart("comment", comment).build();

            httppost.setEntity(reqEntity);

            System.out
                    .println("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: "
                            + resEntity.getContentLength());
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 默认编码utf -8 Obtains character set of the entity, if known.
     *
     * @param entity
     *            must not be null
     * @return the character set, or null if not found
     * @throws ParseException
     *             if header elements cannot be parsed
     * @throws IOException
     * @throws IllegalArgumentException
     *             if entity is null
     */
    public static String getContentCharSet(final HttpEntity entity)
            throws ParseException, IOException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }
        return charset;
    }

    /**
     * 默认编码utf -8 使用jsoup解析，取得meta里的chaset
     */
    public static String getcharsetbydoc(String str) {
        String charset = "";
        Document doc = Jsoup.parse(str);
        Element meta = doc.select(
                "meta[http-equiv=content-type], meta[charset]").first();
        if (meta != null) { // if not found, will keep utf-8 as best attempt
            String foundCharset = null;
            if (meta.hasAttr("http-equiv")) {
                foundCharset = getCharsetFromContentType(meta.attr("content"));
            }
            if (foundCharset == null && meta.hasAttr("charset")) {
                try {
                    if (Charset.isSupported(meta.attr("charset"))) {
                        foundCharset = meta.attr("charset");
                    }
                } catch (IllegalCharsetNameException e) {
                    foundCharset = null;
                }
            }

            if (foundCharset != null && foundCharset.length() != 0
                    && !foundCharset.equals("UTF-8")) { // need to re-decode
                foundCharset = foundCharset.trim().replaceAll("[\"']", "");
                charset = foundCharset;
                doc = null;
            }
        }

        if (StringUtils.isEmpty(charset)) {
            charset = "UTF-8";
        }
        return charset;
    }

    public static String getCharsetFromContentType(String contentType) {
        if (contentType == null)
            return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            charset = charset.replace("charset=", "");
            if (charset.length() == 0)
                return null;
            try {
                if (Charset.isSupported(charset))
                    return charset;
                charset = charset.toUpperCase(Locale.ENGLISH);
                if (Charset.isSupported(charset))
                    return charset;
            } catch (IllegalCharsetNameException e) {
                // if our advanced charset matching fails.... we just take the
                // default
                return null;
            }
        }
        return null;
    }


    /**
     * 发送 get请求
     */
    public String get4h5(String url) {
        String htmlString = "";
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(30000).setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .setStaleConnectionCheckEnabled(true).build();

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();

        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/600.1.3 (KHTML, like Gecko) Version/8.0 Mobile/12A4345d Safari/600.1.4");
            //	httpget.setHeader("Referer","");
            // 复制客户端超时设置
            RequestConfig requestConfig = RequestConfig.copy(
                    defaultRequestConfig).build();
            httpget.setConfig(requestConfig);
            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                System.out.println("--------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: "
                            + entity.getContentLength());

                    // charset处理
                    String charset = getContentCharSet(entity);
                    if (charset != null && charset.length() > 0) {
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        htmlString = EntityUtils.toString(entity, charset);
                    } else {
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        String docData = new String(bytes, "UTF-8");
                        charset = getcharsetbydoc(docData);
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        htmlString = new String(bytes, charset);
                    }
                    System.out.println("charset:" + charset);
                }
                System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlString;
    }
    /**
     * 用于抓取东莞时报
     * @param u
     * @return
     */
    public static String getTimedg(String u) {
        URL url;
        StringBuilder s = new StringBuilder();
        try {
            url = new URL(u);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            // 取得输入流，并使用Reader读取
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));//设置编码,否则中文乱码
            String line;
            while ((line = reader.readLine()) != null){
                s.append(line);
            }
            reader.close();
            // 断开连接
            connection.disconnect();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s.toString();
    }

    /**用于抓取王者荣耀
     * @param url
     * @param refer
     * @return
     */
    public static String get_wzry(String url,String refer){
        String htmlString = "";
        // CloseableHttpClient httpclient = HttpClients.createDefault();
        // 客户请求超时配置
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(3000).setConnectTimeout(3000)
                .setConnectionRequestTimeout(3000)
                .setStaleConnectionCheckEnabled(true).build();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();
        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            // 复制客户端超时设置
            RequestConfig requestConfig = RequestConfig.copy(
                    defaultRequestConfig).build();
            httpget.setConfig(requestConfig);
            httpget.setHeader("Host", "apps.game.qq.com");
            httpget.setHeader("Connection", "keep-alive");
            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
            httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
            httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");
            httpget.setHeader("Cookie", "eas_sid=X1E4A8f059a344t9i9K5l3q2T3; tvfe_boss_uuid=acb2893b4fce0683; mobileUV=1_159149a35c6_60997; RK=OLfWPpAO/E; pac_uid=1_2881637033; verifysession=h0124ade28b476ebe34b23ffce60e8cf0cacd25c120accbad4e9c8ae9bbddfc6a48f6b2ee4561ee203d; h_uid=H07870516e13; ctid=11; sessid=9DD75810-0F83-DEAB-C623-E31B73B00840; aQQ_ajkguid=5E556C03-F6F4-7A2F-35D1-A835CF1FFE2E; pgv_si=s8869601280; aboutVideo_v=0; pgv_pvi=1472780288; pt_local_token=1; qq_slist_autoplay=on; LW_uid=4164h9Q4N9W8z9R7R479q1U3d1; qm_authimgs_id=1; qm_verifyimagesession=h012b10ddfa8a03a3847ff505117efa77510de83adc5f9024515c3f481af88bc51118b2211e2805c05d; _qpsvr_localtk=0.6952905156781566; o_cookie=2881637033; ptui_loginuin=jinyang@021.com; ptisp=cnc; ptcz=b8f03ca1bd992f10cdef3e625e675688a258d19f7bec9ef4e126c6562750ba45; pt2gguin=o2881637033; LW_sid=e1F4M9z5p660S9s313U8O1C3t5; pgv_info=ssid=s1062462040&ssi=s2958297795&pgvReferrer=; pgv_pvid=5041832056");
            httpget.setHeader("Referer",refer);
            System.out.println("executing request " + httpget.getURI());
            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                System.out.println("--------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    System.out.println("Response content length: "
                            + entity.getContentLength());

                    // charset处理
                    String charset = getContentCharSet(entity);
                    if (charset != null && charset.length() > 0) {
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        htmlString = EntityUtils.toString(entity, charset);
                    } else {
                        byte[] bytes = EntityUtils.toByteArray(entity);
                        String docData = new String(bytes, "UTF-8");
                        charset = getcharsetbydoc(docData);
                        if (charset.equalsIgnoreCase("gb2312")) {
                            charset = "GBK";
                        }
                        htmlString = new String(bytes, charset);
                    }
                    System.out.println("charset:" + charset);
                }
                System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return htmlString;
    }


    public static int getStatusCode() {
        return statusCode;
    }
    public static void main(String[] args) throws Exception {
//		try {
//		String str=	get4https("https://m.laonanren.com/xinwen/");
//		System.out.println(str);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		HttpClientUtils httpClientTest = new HttpClientUtils();
//		System.out.println(httpClientTest.get("http://s.weibo.com/top/summary"));
        String url="http://yn.people.com.cn/n2/2018/0109/c372456-31120964.html";
        HttpClientUtils.get (url);
        HttpClientUtils.get4https(url);
        //	System.out.println();
    }

    public static String SendGET(String url,String code){
        String result="";//访问返回结果
        BufferedReader read=null;//读取访问结果
        try {
            //创建url
            URL realurl=new URL(url);
            //打开连接
            URLConnection connection=realurl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //建立连接
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            read = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(),code == null ? "GBK" : code));
            String line;//循环读取
            while ((line = read.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
//			   System.out.println("无法获取！");
        }finally{
            if(read!=null){//关闭流
                try {
                    read.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }
    /**
     * 编码识别工具方法
     *
     *
     */
//
    public static String detect(byte[] content) {
        UniversalDetector detector = new UniversalDetector(null);
        //开始给一部分数据，让学习一下啊，官方建议是1000个byte左右（当然这1000个byte你得包含中文之类的）
        detector.handleData(content, 0, content.length);
        //识别结束必须调用这个方法
        detector.dataEnd();
        //神奇的时刻就在这个方法了，返回字符集编码。
        return detector.getDetectedCharset();
    }
}
