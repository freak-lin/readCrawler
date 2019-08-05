package com.rd.sh.impl;



import com.rd.sh.utils.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;




public class Toutiao {
    private static String as;
    private static String cp;
    private static String[] headers;
    private static String user_agent = "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";
    private static String cookies;
    private static String[] a = {"1590023233148931"};
    private static int number= 0;

    public static void main(String[] args) {
        System.out.println(getResult("1628853364303880"));
    }
    //获取总阅读量
    public static int getResult(String article) {
        number = 0;
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            generateAsCp();
            String url = "https://www.toutiao.com/pgc/ma/?media_id=" + article + "&page_type=1&max_behot_time=0&count=10&version=2&platform=pc&as=" + as + "&cp=" + cp;
            headers = new String[]{user_agent};
            String rt = "";
            String[] ipPort = {};
            try {
                ipPort = ProxyUtil.getProxy().split(":");
//                System.out.println(ipPort.toString());
                rt = GetDoc.getdocByproxy(url, ipPort[0], ipPort[1], 3000, headers, 0,2);
//                rt = GoodHTTP.sendGet(url, headers, "utf-8", 3000, ipPort[0],Integer.parseInt(ipPort[1]));
            } catch (Exception e) {
                try {
                    ipPort = ProxyUtil.getProxy().split(":");
                    rt = GetDoc.getdocByproxy(url, ipPort[0], ipPort[1], 3000, headers, 0,2);
                } catch (Exception e1) {
                    try {
                        ipPort = ProxyUtil.getProxy().split(":");
                        rt = GetDoc.getdocByproxy(url, ipPort[0], ipPort[1], 3000, headers, 0,2);
                    } catch (Exception e2) {
                        System.out.println("多次代理无效");
                        LogUtils.writeLog(url+ "    " +"多次代理无效");
                    }
                }

            }

            cookies = GoodHTTP.getCookies();
            JSONObject ob, json;
            //获取max_behot_time值
            try {
                json = JSONObject.fromObject(rt);
                ob = (JSONObject) json.get("next");
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }

            if (ob == null) {
                return 0;
            }
            JSONArray data = json.getJSONArray("data");
            addCountFromJsonArray(data);

            // 获取分页数据
            Object max_behot_time = ob.get("max_behot_time");
            if (max_behot_time != null) {
                while (true) {
                    max_behot_time = refreshLink(max_behot_time, article);
                    if (max_behot_time == null) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return number;
    }

    //是否原创和pr账号
    public static String[] getDVandOriginal(String url) {
        String dv = "0";
        String original = "0";
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            headers = new String[]{user_agent};
            String rt = GoodHTTP.sendGet(url, headers, "utf-8", 3000);
            JSONObject ob, json;
            //判断是否是大v
            if (rt.contains("dv:true")) {
                dv = "1";
            } else {
                dv = "0";
            }
            //判断是否是原创
            try {
                String linkId = "";
                if (url.contains("mid=")) {
                    linkId = url.split("mid=")[1];
                } else {
                    linkId = url.split("user/")[1].split("/#mid=")[0];
                }
                generateAsCp();
                String jsurl = "https://www.toutiao.com/pgc/ma/?media_id=" + linkId + "&page_type=1&max_behot_time=0&count=10&version=2&platform=pc&as=" + as + "&cp=" + cp;
                headers = new String[]{user_agent};
                String rt2 = GoodHTTP.sendGet(jsurl, headers, "utf-8", 3000);
                cookies = GoodHTTP.getCookies();
                JSONArray jsonArray = JSONObject.fromObject(rt2).getJSONArray("data");
                for (int i = 0; i <= jsonArray.size() - 1; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String articleUrl = jsonObject.getString("source_url");
                    String articleData = GoodHTTP.sendGet(articleUrl, headers, "utf-8", 3000);
                    if (articleData.contains("isOriginal: true")) {
                        original = "1";
                        break;
                    }
                }
                return new String[]{dv,original};
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       return new String[]{dv, original};
    }

    private static void addCountFromJsonArray(JSONArray data) throws IOException {
        for (int arrayIndex = 0; arrayIndex <= data.size() - 1; arrayIndex++) {
            JSONObject jsonData = data.getJSONObject(arrayIndex);
            String conut = jsonData.getString("go_detail_count");
            if (conut.contains("万")) {
                conut = conut.split("万")[0];
                float k = Float.valueOf(conut) * 10000;
                conut = String.valueOf((int)k);
            }
            String sourceUrl = jsonData.getString("source_url");
            String title = jsonData.getString("title");
            FileWriter fw = null;
            int nu = Integer.valueOf(conut);
            number += nu;
        }
    }

    private static void generateAsCp() {
        DecimalFormat decimalFormat = new DecimalFormat("###0");// 格式化设置
        String e = decimalFormat.format(Math.floor(new Date().getTime() / 1e3));
        String r = Long.toHexString(Long.valueOf(e)).toUpperCase();
        char[] r1 = r.toCharArray();
        String t = MD5.GetMD5Code(String.valueOf(e)).toUpperCase();
        if (8 != r1.length) {
            as = "479BB4B7254C150";
            cp = "7E0AC8874BB0985";
        }
        char[] o = t.substring(0, 5).toCharArray(), i = t.substring(t.length() - 5).toCharArray();
        String n = "";
        for (int s = 0; s < 5; s++) {
            n += String.valueOf(o[s]) + String.valueOf(r1[s]);
        }
        String a = "";
        for (int c = 0; c < 5; c++) {
            a += String.valueOf(r1[c + 3]) + String.valueOf(i[c]);
        }
        as = "A1" + n + r.substring(r1.length - 3);
        cp = r.substring(0, 3) + a + "E1";
    }

    private static Object refreshLink(Object max_behot_time, String link) {
        String url = "https://www.toutiao.com/pgc/ma/?media_id=" + link + "&page_type=1&max_behot_time=" + max_behot_time + "&count=10&version=2&platform=pc&as=" + as + "&cp=" + cp;
        String rt = "";
        String[] ipPort = {};
        try {
            ipPort = ProxyUtil.getProxy().split(":");
            rt = GetDoc.getdocByproxy(url, ipPort[0], ipPort[1], 3000, headers, 0,2);
        } catch (Exception e) {
            try {
                ipPort = ProxyUtil.getProxy().split(":");
                rt = GetDoc.getdocByproxy(url, ipPort[0], ipPort[1], 3000, headers, 0,2);
            } catch (Exception e1) {
                try {
                    ipPort = ProxyUtil.getProxy().split(":");
                    rt = GetDoc.getdocByproxy(url, ipPort[0], ipPort[1], 3000, headers, 0,2);
                } catch (Exception e2) {
                    System.out.println("多次代理无效");
                }
            }

        }
        cookies = GoodHTTP.getCookies();
        headers = new String[]{cookies, user_agent};

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JSONObject json = null;
        JSONObject ob = null;
        try {
            json = JSONObject.fromObject(rt);
            ob = (JSONObject) json.get("next");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (ob == null || json.getJSONArray("data").size() == 0) {
            return null;
        }
        try {
            max_behot_time = ob.get("max_behot_time");
            JSONArray data = json.getJSONArray("data");
            addCountFromJsonArray(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return max_behot_time;
    }

}

