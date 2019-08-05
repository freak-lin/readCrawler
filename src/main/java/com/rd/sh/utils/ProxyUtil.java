package com.rd.sh.utils;

public class ProxyUtil {

    public static String getProxy(){
        return RedisUtils.scan("proxy_pool");
    }

}
