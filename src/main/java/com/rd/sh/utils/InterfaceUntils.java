package com.rd.sh.utils;


import com.rd.sh.common.HbaseConnmini;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class InterfaceUntils {
    private static String[] headers = {"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36"};
    //接受数据接口
    public static JSONObject getDate(String pagesize, String pageIndex) {
        Map<String,String> param=new HashMap<>();
        param.put("pageSize",pagesize);
        param.put("startTime", HbaseConnmini.START_TIME);
        param.put("pageIndex",pageIndex);
        param.put("endTime",HbaseConnmini.END_TIME);
        param.put("t",String.valueOf(System.currentTimeMillis()));
        param.put("token",md5Sign(param,"KLJSAKLFJ35431*&&76$%"));
        //输出参数记录日志
        LogUtils.writeLog("获取到的数据："+param.toString());
//        String url1 = "http://172.18.250.87:8581/east/cooperation/auxiliary/getUserAuxiliary?pageSize="+param.get("pageSize")+ "&pageIndex="+param.get("pageIndex")+ "&t=" + param.get("t") + "&token=" + param.get("token") + "&startTime=" + param.get("startTime") + "&endTime=" + param.get("endTime");
        String url1 = "http://mpserver.tt.cn/east/cooperation/auxiliary/getUserAuxiliary?pageSize="+param.get("pageSize")+ "&pageIndex="+param.get("pageIndex")+ "&t=" + param.get("t") + "&token=" + param.get("token") + "&startTime=" + param.get("startTime") + "&endTime=" + param.get("endTime");
        String data = "";
        try {
            data = GoodHTTP.sendGet(url1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!data.isEmpty()) {
            return JSONObject.fromObject(data);
        }
        return null;
    }
    //推送数据接口
    public static JSONObject postDate(String userid, String link, String mediaName, String yc, String largeV, String readPv, String istt) {
        Map<String,String> param=new HashMap<>();
        param.put("userid",userid);
        param.put("link",link);
        param.put("mediaName",mediaName);
        param.put("yc",yc);
        param.put("largeV",largeV);
        param.put("read_pv",readPv);
        param.put("istt",istt);
        param.put("t",String.valueOf(System.currentTimeMillis()));
        param.put("token",md5Sign(param,"KLJSAKLFJ35431*&&76$%"));
//        String url1 = "http://172.18.250.87:8581/east/cooperation/auxiliary/submitAuxiliaryData";
        String url1 = "http://mpserver.tt.cn/east/cooperation/auxiliary/submitAuxiliaryData";
        String data = "";
        try {
            data = HttpClientUtils.requestPost(url1, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!data.isEmpty()) {
            return JSONObject.fromObject(data);
        }
        return null;
    }
    //签名算法
    public static String md5Sign(Map<String, String> map, String token){
        StringBuffer sb = new StringBuffer();
        sb.append(token);
        //参数排序
        Map<String,String> treeMap = new TreeMap<>(map);
        for(String key : treeMap.keySet()){
            sb.append(key);
            String value = treeMap.get(key);
            if(null != value){
                sb.append(value.trim());
            }
        }
        sb.append(token);
        return MD5.GetMD5Code(sb.toString());
    }
}
