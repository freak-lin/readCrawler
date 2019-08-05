package com.rd.sh.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by taojinhou on 2018/3/10.
 */
public class RecommendUtils_V33 {
//	https://aweme.snssdk.com/aweme/v1/comment/list/?aweme_id=6709059424222252300&cursor=0&count=20&comment_style=2&ts=1562140309&app_type=normal&openudid=9c05bce3d5d71a96&version_name=1.7.0&device_type=Note4&ssmix=a&iid=77512714386&os_api=19&device_id=54704245180&resolution=720*1280&device_brand=iPhone&aid=1128&manifest_version_code=170&app_name=aweme&_rticket=1562140309840&os_version=4.4.4&device_platform=android&version_code=170&update_version_code=1702&ac=wifi&dpi=320&uuid=869437020727610&language=zh&channel=wandoujia&as=a105a5819549bdcefc&cp=599fd25054c41de7e1
	
	private static String versionName = "1.7.0";
	private static String versionCode = versionName.replaceAll("\\.", "");
	public static Map<String, String> params = new LinkedHashMap<String, String>() {
		{
			// put("ch_id","1623621373387779");
			// put("cursor","0");
			put("count", "20");
//			put("type", "5");
			put("comment_style", "2");
			put("app_type", "normal");
			put("openudid", "9c05bce3d5d71a96");
			put("version_name", "1.7.0");
			put("device_type","Note4");
			put("ssmix","a");
			put("aid","1128");
//			put("iid","77512714386");
			put("os_api","19");
//			put("device_id", "54704245180");
			put("resolution","720*1280");
			put("device_brand","iPhone");
			put("manifest_version_code","170");
			put("app_name","aweme");
			put("os_version","4.4.4");
			put("device_platform","android");
			put("version_code","170");
			put("update_version_code","1702");
			put("ac","wifi");
			put("dpi","320");
			put("uuid","869437020727610");
			put("language","zh");
			put("channel","wandoujia");
		}
	};

	public static String getRecommendAPI(String aweme_id, int cursor) {
		params.put("device_id", getRandomNumber(11)); // 随机11位设备号
		/* 处理参数 */
		params.put("aweme_id", aweme_id);
		params.put("cursor", String.valueOf(cursor));
		int ts = (int) (System.currentTimeMillis() / 1000);
		params.put("ts", String.valueOf(ts));
		params.put("_rticket", String.valueOf(System.currentTimeMillis()));
		TreeMap<String, String> paramsTree = new TreeMap<>(params); // 用TreeMap排序
		paramsTree.put("rstr", "efc84c17");
		// 将待加密字段所有+换传a，该步由动态调试数据反推，不一定准确，同时也可能存在其他字符替换
		Collection<String> values = paramsTree.values();
		StringBuilder  paramsSb= new StringBuilder();
		for (String value : values) {
			paramsSb.append(value);
		}
		String encode = paramsSb.toString().replaceAll("\\+", "a");
		String asAndCp = LibUserinfoSo.getName(ts, encode);

		/* 拼接url */
		StringBuilder sBuilder = new StringBuilder();
		// sBuilder.append("http://api.amemv.com/aweme/v1/feed/?");
		sBuilder.append("https://aweme.snssdk.com/aweme/v1/comment/list/?");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		int halfLen = asAndCp.length() >> 1;
		sBuilder
				// .append("ts=").append(ts)
				.append("as=").append(asAndCp.substring(0, halfLen)).append("&cp=").append(asAndCp.substring(halfLen));

		return sBuilder.toString();
	}

	public static void main(String[] args) {
		int cursor = 0;
		while (true) {
//			已经添加你19次你也没通过
//			https://www.iesdouyin.com/share/video/6702754517114490120/?region=CN&mid=6654664457916009229&u_code=g1c6739e&titleType=title&utm_source=copy_link&utm_campaign=client_share&utm_medium=android&share_app_name=aweme&share_iid=77512714386
			String string = getRecommendAPI("6676981651567955204", cursor);
			System.err.println(string);
			String contentGet = HttpClientUtils.sendGet(string, "User-Agent=okhttp/3.8.1&Accept-Encoding=gzip");
//					.get(string,new String[] { "User-Agent=okhttp/3.8.1", "Accept-Encoding=gzip" });
			JSONObject jsonObject = JSONObject.fromObject(contentGet);
			System.out.println(jsonObject);
			if (!jsonObject.containsKey("comments")) {
				break;
			}
			JSONArray comments = jsonObject.getJSONArray("comments");
			if (comments.isEmpty()) {
				break;
			}
			for (Object object : comments) {
				JSONObject comment = (JSONObject) object;
				String text = comment.getString("text");
				String biaoqing="";
					Pattern pattern1 = Pattern.compile("\\[.*\\]");
					Matcher m1 = pattern1.matcher(text);
					if (m1.find()) {
						biaoqing = m1.group();
						text= text.replace(biaoqing, "");
					}
					text=text.replace("😄", "");
					System.out.println(text);
					if (text.length()>10) {
						
					}
			}
			cursor+=20;
			String has_more = jsonObject.getString("has_more");
			if (!"1".equals(has_more)) {
//				System.out.println(jsonObject);
				break;
			}
//			System.out.println(contentGet);
		}
	}

	private static String getRandom(int length, String code) {
		StringBuilder builder = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			builder.append(code.charAt(random.nextInt(code.length())));
		}

		return builder.toString();
	}

	public static String getRandomNumber(int length) {
		return getRandom(length, "0123456789");
	}

}
