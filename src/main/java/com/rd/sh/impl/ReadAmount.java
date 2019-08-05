package com.rd.sh.impl;


import com.rd.sh.utils.InterfaceUntils;
import com.rd.sh.utils.LogUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class ReadAmount {


    public static void main(String[] args) {
        for (int j = 1; ; j++) {
            try {
                JSONObject jsonObject = InterfaceUntils.getDate("20", String.valueOf(j));
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                System.out.println("数组数量" + jsonArray.size());
                for (int i = 0; i <= jsonArray.size() - 1; i++) {
                    try {
                        System.out.println("索引：" + i);
                        String dv = "";
                        String original = "";
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        if (jsonObject1.getString("crawled").contains("1")) {
                            System.out.println("数据已经爬取过");
                            continue;
                        }
                        if (jsonObject1.getString("platformName").contains("头条号")) {
                            String link = jsonObject1.getString("homeLink");
                            String linkId = "";
                            if (link.contains("mid=")) {
                                linkId = link.split("mid=")[1];
                            } else {
                                linkId = link.split("user/")[1].split("/#mid=")[0];
                            }
                            //获取阅读量
                            int num = Toutiao.getResult(linkId);

                            //获取是否是原创，大v  String[0]是大v String[1]是原创
                            String[] isresult = Toutiao.getDVandOriginal(link);

                            System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                            System.out.println(jsonObject1.getString("userid") + "    " + jsonObject1.getString("homeLink") + "    " + jsonObject1.getString("userid") + "    " + isresult[1] + "    " + isresult[0] + "    " + String.valueOf(num) + "    " + "1");
                            JSONObject result = InterfaceUntils.postDate(jsonObject1.getString("userid"), jsonObject1.getString("homeLink"), jsonObject1.getString("weMediaName"), isresult[1], isresult[0], String.valueOf(num), "1");
                            if (!result.getString("code").contains("0000")) {
                                LogUtils.writeLog("插入失败，参数是：" + jsonObject1.getString("userid") + "    " + jsonObject1.getString("homeLink") + "    " + jsonObject1.getString("userid") + "    " + isresult[1] + "    " + isresult[0] + "    " + String.valueOf(num) + "    " + "1");
                            }
                            System.out.println(result.toString());
                        } else if (jsonObject1.getString("platformName").contains("百家号")) {
                            String link = jsonObject1.getString("homeLink");
                            //获取阅读量
                            int num = Baijia.getReadCount(link);
                            //获取是否是原创，大v  String[0]是大v String[1]是原创
                            String[] isresult = Baijia.getStatus(link);
                            System.out.println("--------------------------------------------------------------------------------------------------------------------------");
                            System.out.println(jsonObject1.getString("userid") + "    " + jsonObject1.getString("homeLink") + "    " + jsonObject1.getString("userid") + "    " + isresult[1] + "    " + isresult[0] + "    " + String.valueOf(num) + "    " + "2");
                            JSONObject result = InterfaceUntils.postDate(jsonObject1.getString("userid"), jsonObject1.getString("homeLink"), jsonObject1.getString("weMediaName"), isresult[1], isresult[0], String.valueOf(num), "2");
                            if (!result.getString("code").contains("0000")) {
                                LogUtils.writeLog("插入失败，参数是：" + jsonObject1.getString("userid") + "    " + jsonObject1.getString("homeLink") + "    " + jsonObject1.getString("userid") + "    " + isresult[1] + "    " + isresult[0] + "    " + String.valueOf(num) + "    " + "1");
                            }
                        } else {
                            LogUtils.writeLog("获取数据失败" + jsonObject.toString());
                        }
                    } catch (Exception e) {
                        LogUtils.writeLog("程序错误：" + e);
                        e.printStackTrace();
                    }
                }
                if (jsonArray.size() < 20) {
                    break;
                }
            } catch (Exception e) {
                LogUtils.writeLog("接口数据请求报错"+e.toString());
            }
            }
    }

}
