package com.rd.sh.utils;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpClientUtils {
	private static int statusCode = 200;
	private static final Pattern charsetPattern = Pattern
			.compile("(?i)\\bcharset=\\s*(?:\"|')?([^\\s,;\"']*)");

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
	public static String requestPost(String url, Map<String,String> _paramsmap) {
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
		httppost.setHeader("clientVersion","3.5.3");
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
//					System.out.println("--------------------------------------");
					content = EntityUtils.toString(entity, "UTF-8");
//					System.out.println("Response content: " + content);
//					System.out.println("--------------------------------------");
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
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
	 * 处理人人视频的，获取视频流的地址的信息
	 */
	@SuppressWarnings("finally")
	public static String requestPostTORR(String url, Map<String, String> _paramsmap) {
		String content = "";
		// 客户请求超时配置
		RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(10 * 1000).setConnectTimeout(10 * 1000).setConnectionRequestTimeout(10 * 1000).setStaleConnectionCheckEnabled(true).build();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
		// 创建httppost
		HttpPost httppost = new HttpPost(url);
		// 复制客户端超时设置
		RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();
		httppost.setConfig(requestConfig);
		httppost.setHeader("clientVersion", "3.5.0'");
		httppost.setHeader("clientType", "web");
		httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
		httppost.setHeader("Host", "web.rr.tv");
		httppost.setHeader("Cookie", "OUTFOX_SEARCH_USER_ID_NCOO=325489809.53118414; UM_distinctid=15e32965dd44c5-0487b7b5adf109-63171875-1fa400-15e32965dd5744; JSESSIONID=F1B434AEE89BE160631BD756DE6F203D");
		httppost.setHeader("Connection", "keep-alive");
		httppost.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		httppost.setHeader("Accept", "application/json, text/plain, */*");
		httppost.setHeader("Referer", "http://rr.tv/");

		// 创建参数队列
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (Entry<String, String> paramsentry : _paramsmap.entrySet()) {
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
					content = EntityUtils.toString(entity, "UTF-8");
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
	public String get(String url) {
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
			httpget.setHeader("Referer","https://www.baidu.com");
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
	 * 第一视频网
	 */
	@SuppressWarnings("finally")
	public static String requestPostfirstvideo(String url, Map<String,String> _paramsmap) {
		String content = "";
		// 客户请求超时配置
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setSocketTimeout(10 * 1000).setConnectTimeout(10 * 1000)
				.setConnectionRequestTimeout(10 * 1000)
				.setStaleConnectionCheckEnabled(true).build();
		// 创建默认的httpClient实例.
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
		HttpPost httppost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();
		httppost.setConfig(requestConfig);
		httppost.setHeader("application/json","text/javascript, */*; q=0.01");
		httppost.setHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
		httppost.setHeader("Cookie", "yu=3131444189052; UM_distinctid=15f6c76b97f62a-0db41bf2d51e26-63171875-1fa400-15f6c76b9806f0; PHPSESSID=ud3d36nvcubrq2t84etubk8ha2; Hm_lvt_cb44eb450c53c91a1cc1d2511f5919b3=1509353241,1509427257; Hm_lpvt_cb44eb450c53c91a1cc1d2511f5919b3=1509428660; CNZZDATA1255096340=42102573-1509352544-%7C1509427255");
		httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
		httppost.setHeader("Host", "www.v1.cn");
		httppost.setHeader("Origin", "http://www.v1.cn");
		httppost.setHeader("Referer", "http://www.v1.cn/caijing/");
		
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
					content = EntityUtils.toString(entity, "UTF-8");
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
	 * 获取今日头条视频的详情页
	 */
	public static Document getdoc4Jinritoutiao(String url, int timeout, int waittime, int retrytime) {
		Document Doc = null;
		for (int i = 0; i < retrytime; i++) {
			try {
				if (waittime > 0) {
					Thread.sleep(waittime);
				}
				Doc = Jsoup.connect(url)
				.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
				.header("accept-encoding", "gzip, deflate, br")
				.header("accept-language", "zh-CN,zh;q=0.9")
				.header("cache-control", "max-age=0")
//				.header("cookie", "UM_distinctid=16057f55931847-0470414af1deaf-5e183017-1fa400-16057f55932a41; WEATHER_CITY=%E5%8C%97%E4%BA%AC; _ga=GA1.2.98237382.1513304186; tt_webid=6501459265592051214; _gid=GA1.2.1602561276.1516773290; CNZZDATA1262382642=1900602188-1513300576-%7C1516840921")
				.header("upgrade-insecure-requests", "1")
				.header("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36")
				.get();
				if (Doc != null) {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		return Doc;
	}

	/**
	 * 获取cookie中的的值
	 */
	private static String getCookie(String url1) throws MalformedURLException, IOException {
		String cna = "";
		URL url = new URL(url1);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		Map<String, List<String>> map = conn.getRequestProperties();
		Map<String, List<String>> map = conn.getHeaderFields();
		Set<String> keySet = map.keySet();
		for (String key : keySet) {
			System.out.println("key:"+key);
			System.out.println("value:"+map.get(key));
			if (key == null || !key.equals("Set-Cookie")) {
				continue;
			}
			List<String> list = map.get(key);
			for (String it : list) {
				if (it.contains("cna")) {
					cna = it.substring(it.indexOf("cna") + 4, it.indexOf(";"));
				}
			}
		}
		return cna;
	}
	
	/**
	 * 根据uu，uv 获取视频jsonUrl
	 * 车讯网视频
	 */
	public static String getchexunJsonUrl(String uu, String vu) {
		String ran = System.currentTimeMillis() / 1000 + "";
		String cf = "html5";
		String uuid = "E77D6256D302FB85FB41585DA20C7CC3_0";
		String pet = System.currentTimeMillis() + "";
		String sign = MD5(cf + uu + vu + ran + "fbeh5player12c43eccf2bec3300344");
		String pageUrl = "http://yuntv.letv.com/bcloud.html?uu=" + uu + "&vu=" + vu;
		String page_url = "";
		try {
			page_url = URLEncoder.encode(pageUrl, "iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String jsonUrl = "http://api.letvcloud.com/gpc.php?format=json&ver=2.4&cf=" + cf + "&ran=" + ran + "&pver=H5_Vod_20170406_4.8.3&bver=chrome62.0.3202.89&uuid=" + uuid + "&pf=html5&spf=0&uu=" + uu + "&vu=" + vu + "&lang=zh_CN&pet=" + pet + "&sign=" + sign + "&page_url=" + page_url;
		return jsonUrl;
	}
	
	public static String MD5(String key) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] btInput = key.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 获取易车视频的json
	 */
	public static Document getYiCheJson(String url, int timeout, int waittime, int retrytime) {
		Document Doc = null;
		for (int i = 0; i < retrytime; i++) {
			try {
				if (waittime > 0) {
					Thread.sleep(waittime);
				}
				Doc = Jsoup.connect(url)
				.header("Accept", "application/json, text/javascript, */*; q=0.01")
				.header("Accept-Encoding", "gzip, deflate")
				.header("Accept-Language", "zh-CN,zh;q=0.9")
				.header("Connection", "keep-alive")
				.header("Cookie", "UserGuid=f317c426-c810-44dc-87e5-ab7aaf8705a8; _dc3c=1; dc_search325=; CIGDCID=9c7b56f93ca092aaaac284a48edf74f6; locatecity=310100; bitauto_ipregion=180.173.46.1%3a%e4%b8%8a%e6%b5%b7%e5%b8%82%3b2401%2c%e4%b8%8a%e6%b5%b7%2cshanghai; dcad325=; dmt325=1%7C0%7C0%7Chao.yiche.com%2Fwenzhang%2F440289%2F%3Fid%3D440289%26type%3D21%7Ci.yiche.com%2Fu23595338%2F; dm325=11%7C1514962794%7C0%7C%7C%7C%7C%7C1513581563%7C1513581563%7C1514520284%7C1514962794%7C9c7b56f93ca092aaaac284a48edf74f6%7C0%7C%7C")
				.header("Host", "i.yiche.com")
				.header("Referer", "http://i.yiche.com/u23595338/")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36")
				.header("X-Requested-With", "XMLHttpRequest")
				.ignoreContentType(true)
				.get();
				if (Doc != null) {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		return Doc;
	}
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
	
	public static void main(String[] args) throws Exception {
		HttpClientUtils httpClientTest = new HttpClientUtils();
		String url = "https://www.ixigua.com/a6508943816928526851";
		String cookie = HttpClientUtils.getCookie(url);
		System.out.println(cookie);
	}
}