package com.rd.sh.utils;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoodHTTP {

	private static String cookie = "";
	
	private static HttpHost proxy = null;

	private static CookieStore cookieStore = new BasicCookieStore();


//	public  static void setCookie(String cookie){
//		String cookieStr = cookie.split(":")[1];
//		String[] cookieArr = cookie.split(";");
//		for(String perCookie:cookieArr){
//			String[] split = perCookie.split("=");
//			BasicClientCookie c = new BasicClientCookie(split[0],split[1]);
//			cookieStore.addCookie(c);
//		}
//	}
	
	public static void setHeader(HttpGet get, String[] headers) { ;
		get.addHeader("Connection", "close");
		if (headers.length == 0) {
			return;
		}
		for (String header : headers) {
			if(header.equals("")){
				continue;
			}
			
			String[] split = header.split(":", 2);
			if (split[0].trim().equals("Cookie")) {
				cookie = split[1];
			}
			get.setHeader(split[0], split[1]);
		}
	}
	
	
	public static String sendGet(String url) throws IOException {
		return sendGet(url, null, null, 3000);
	}

	public static String sendGet(String url, String[] headers, String charset, int timeout,String ip,int port) throws IOException {
		String html = "";
		Builder custom = RequestConfig.custom();
		RequestConfig config = custom.setConnectionRequestTimeout(timeout).setConnectTimeout(timeout)
				.setSocketTimeout(timeout).setAuthenticationEnabled(false)
				.setCookieSpec(CookiePolicy.BROWSER_COMPATIBILITY)
				.setProxy(new HttpHost(ip,port))
				.build();
		CookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore)
				.setDefaultRequestConfig(config).build();
		System.out.println("request:"+ip+":"+port +"\t" + url);

		HttpGet get = new HttpGet(url);
		HttpClientContext context = HttpClientContext.create();
		if (headers != null) {
			setHeader(get, headers);
		}
		RequestConfig copy = RequestConfig.copy(config).build();
		get.setConfig(copy);
		CloseableHttpResponse response = httpclient.execute(get,context);
		HttpEntity entity = response.getEntity();
		if (charset != null) {
			html = EntityUtils.toString(entity, charset);
		} else {
			html = getHtml(entity);
		}
		return html;

	}
	
	
	public static String sendGet(String url, String[] headers, String charset, int timeout) throws ClientProtocolException, IOException {
		String html = "";

		Builder custom = RequestConfig.custom();


		RequestConfig config = custom.setConnectionRequestTimeout(timeout).setConnectTimeout(timeout)
				.setSocketTimeout(timeout).setAuthenticationEnabled(false)
				.setCookieSpec(CookiePolicy.BROWSER_COMPATIBILITY)
//				.setProxy(new HttpHost("127.0.0.1",8888,"https"))
				.build();

		CookieStore cookieStore = new BasicCookieStore();

//		cookieStore.addCookie(new BasicClientCookie("aaa","aaa"));
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore)
				.setDefaultRequestConfig(config).build();
//		httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS,true);
		System.out.println("request:" + url);

		HttpGet get = new HttpGet(url);
		HttpClientContext context = HttpClientContext.create();
		if (headers != null) {
			setHeader(get, headers);
		}

//		System.out.println(Arrays.toString(get.getAllHeaders()));

		RequestConfig copy = RequestConfig.copy(config).build();
		get.setConfig(copy);
		CloseableHttpResponse response = httpclient.execute(get,context);
//		context.getCookieStore().getCookies().forEach(System.out::println);
		HttpEntity entity = response.getEntity();
//		System.out.println(response.getStatusLine());
//		System.out.println("contentlength:" + entity.getContentLength());
		if (charset != null) {
//			System.out.println("charset:" + charset);
			html = EntityUtils.toString(entity, charset);
		} else {
			html = getHtml(entity);
		}
		return html;
	}
	
	
	public static String sendIgnoreCookiesGet(String url, String[] headers, String charset, int timeout) throws ClientProtocolException, IOException {
		String html = "";

		Builder custom = RequestConfig.custom();
		if(proxy != null){
			custom.setProxy(proxy);
		}
		
		
		
		RequestConfig config = custom.setConnectionRequestTimeout(timeout).setConnectTimeout(timeout)
				.setSocketTimeout(timeout).setAuthenticationEnabled(false)
				.setCookieSpec(CookiePolicy.IGNORE_COOKIES)
				.build();
		
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore)
				.setDefaultRequestConfig(config).build();

		System.out.println("request:" + url);

		HttpGet get = new HttpGet(url);
		if (headers != null) {
			setHeader(get, headers);
		}
		RequestConfig copy = RequestConfig.copy(config).build();
		get.setConfig(copy);

		CloseableHttpResponse response = httpclient.execute(get);
		HttpEntity entity = response.getEntity();
//		System.out.println(response.getStatusLine());
//		System.out.println("contentlength:" + entity.getContentLength());
		if (charset != null) {
//			System.out.println("charset:" + charset);
			html = EntityUtils.toString(entity, charset);
		} else {
			html = getHtml(entity);
		}
		return html;
	}
	
	public static String getHtml(HttpEntity entity) throws IOException {
		String charset = "";

		// 第一种方式获取
		byte[] bytes = EntityUtils.toByteArray(entity);
		// charset = detect(bytes);

		if (charset == null || charset.equals("")) {
			// 第二种方式获取
			HeaderElement[] elements = entity.getContentType().getElements();
			for (HeaderElement e : elements) {
				NameValuePair parameterByName = e.getParameterByName("charset");
				if (parameterByName != null) {
					charset = parameterByName.getValue();
				}
			}

			if (charset == null || charset.equals("")) {
				// 第三种方式获取
				String temp_h = new String(bytes, "utf-8");
				charset = getCharsetByHtml(temp_h);
			}

		}

		if (!charset.equals("")) {
			if (charset.toLowerCase().startsWith("gb")) {
				charset = "gbk";
			} else if (charset.toLowerCase().startsWith("utf")) {
				charset = "utf-8";
			}
		} else {
			charset = "utf-8";
		}

//		System.out.println("charset:" + charset);

		return new String(bytes, charset);
	}
	
	private static String getCharsetByHtml(String html) {
		String charset = "";
		Pattern p = Pattern.compile("<meta.*charset=\"?([\\w-]*)\"?.*/?>");
		Matcher m = p.matcher(html);
		if (m.find()) {
			charset = m.group(1);
		} else {
			System.out.println("页面没有显示编码");
		}

		charset = charset.replace("\"", "");
		return charset;
	}
	
	
	public static String post(String url,String[] headers,String param,int timeOut) throws IOException {
		
		System.out.println("post:"+url+"\tparams:"+param);
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置头信息
			for(String head:headers) {
				String[] headSplit = head.split(":",2);
				if(headSplit.length==2) {
					conn.setRequestProperty(headSplit[0],headSplit[1]);
				}
			}
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			conn.setConnectTimeout(timeOut);
			conn.setReadTimeout(timeOut);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			System.out.println("发送 POST 请求出现异常！");
			throw e;
		}finally {
			// 关闭输出流、输入流
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		
		return result;
	}
	
	public static String post(String url,String[] headers,String params,int timeOut,String charSet,boolean isRaw) throws IOException {
		
		
		System.out.println("post:"+url+"\tparams:"+params);
		HttpPost post = new HttpPost(url);
		if(isRaw){
			try {
				StringEntity uefEntity = new StringEntity(params);
				post.setEntity(uefEntity);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			String[] entity = params.split("&");
			List<NameValuePair> formparams = new ArrayList<NameValuePair>(entity.length);
			for(int i = 0; i < entity.length; i++) {
				String[] pair = entity[i].split("=", 2);
				formparams.add(new BasicNameValuePair(pair[0], pair[1]));
			}

			UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams);
			post.setEntity(uefEntity);
		}

		Builder custom = RequestConfig.custom();
		if(proxy != null){
			custom.setProxy(proxy);
		}
		
		RequestConfig config = custom.setConnectionRequestTimeout(timeOut).setConnectTimeout(timeOut)
				.setSocketTimeout(timeOut).setAuthenticationEnabled(false)
				.setCookieSpec(CookiePolicy.BROWSER_COMPATIBILITY)
				.build();
		
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore)
				.setDefaultRequestConfig(config).setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();

		if (headers != null && headers.length != 0) {
			post.addHeader("Connection", "close");
			for (String header : headers) {
				if(header.equals("")){
					continue;
				}
				String[] split = header.split(":", 2);
				if (split[0].trim().equals("Cookie")) {
					cookie = split[1];
				}
				post.addHeader(split[0], split[1]);
			}
		}

		CloseableHttpResponse response = httpclient.execute(post);
		
		
		
		return getResponseEntity(response,charSet);
	}



	
	private static String getResponseEntity(CloseableHttpResponse response,String charset) throws IOException{
		String html = "";
		HttpEntity entity = response.getEntity();
		try{
			if (charset != null) {
				System.out.println("charset:" + charset);
				html = EntityUtils.toString(entity, charset);
			} else {
				html = getHtml(entity);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new IOException();
		}finally{
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return html;
	}
	
	
	/**
	 * 忽略证书请求数据
	 * @param url
	 * @param headers
	 * @param charset
	 * @param timeout
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	public static String sendGetIgnoreCRT(String url, String[] headers, String charset, int timeout) throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		String html = "";

		Builder custom = RequestConfig.custom();
		if(proxy != null){
			custom.setProxy(proxy);
		}
		
		
		
		RequestConfig config = custom.setConnectionRequestTimeout(timeout).setConnectTimeout(timeout)
				.setSocketTimeout(timeout).setAuthenticationEnabled(false)
				.setCookieSpec(CookiePolicy.BROWSER_COMPATIBILITY)
				.build();
		
		
		//信任所有
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
			// 信任所有
			public boolean isTrusted(X509Certificate[] chain, String authType) {
				return true;
			}
		}).setSecureRandom(new SecureRandom()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
		CloseableHttpClient	httpclient =  HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCookieStore(cookieStore).setDefaultRequestConfig(config).build();

		System.out.println("request:" + url);

		HttpGet get = new HttpGet(url);
		if (headers != null) {
			setHeader(get, headers);
		}
		RequestConfig copy = RequestConfig.copy(config).build();
		get.setConfig(copy);

		CloseableHttpResponse response = httpclient.execute(get);
		HttpEntity entity = response.getEntity();
		if (charset != null) {
			html = EntityUtils.toString(entity, charset);
		} else {
			html = getHtml(entity);
		}
		return html;
	}
	public static String getCookies() {
		StringBuilder sb = new StringBuilder("Cookie:");
		List<Cookie> cookies = cookieStore.getCookies();
		if (cookies.size() == 0) {
			return "";
		}
		for (Cookie ce : cookieStore.getCookies()) {
			sb.append(ce.getName() + "=" + ce.getValue() + ";");
		}
		return sb.substring(0, sb.lastIndexOf(";"));
	}

}
