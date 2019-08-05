package com.rd.sh.utils;


import com.rd.sh.common.HbaseConnmini;
import org.apache.commons.io.FileUtils;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RedisUtils {
    public static String host = HbaseConnmini.REDIS_HOST;
    public static int port = HbaseConnmini.REDIS_PORT;
    static Jedis jedis;

    static {
        jedis = new Jedis(host,port);
    }

    public static void pushIds(File f,String target) throws IOException {
        List<String> idList = FileUtils.readLines(f);
        int count = 1;
        while(true){
            List<String> tmp = new ArrayList<>();
            if(count*10000-1 < idList.size()-1){
                tmp = idList.subList((count-1)*10000,count*10000-1);
                jedis.lpush(target, tmp.toArray(new String[tmp.size()]));
            }else{
                tmp = idList.subList((count-1)*10000,idList.size()-1);
                jedis.lpush(target, tmp.toArray(new String[tmp.size()]));
                break;
            }

            count ++;
        }

    }

    public static  synchronized void lpush(String target,String content){
        jedis.lpush(target,content);
    }

    public static synchronized String rpopid(String target){
        return jedis.rpop(target);
    }


    public static synchronized Long getSize(String target){
        return jedis.llen(target);
    }

    public static synchronized String getValue(String target,String key){
        return jedis.hget(target,key);
    }

    public static synchronized void setValue(String target,String key,String value){
        jedis.hset(target,key,value);
    }

    public static synchronized String scan(String target){
        return jedis.srandmember(target);
    }

    public static void main(String[] args) throws IOException {

        //push京东数据
//        pushIds(new File("/data/comments/crawlerconf/jd_product_id.txt"),"zy_comments_jd");
        System.out.println(getSize("zy_comments_jd"));


        //push抖音数据
//        pushIds(new File("/data/comments/crawlerconf/aweme-1.log"),"zy_comments_douyin");
        System.out.println(getSize("zy_comments_douyin"));

        //push网易云数据
//        pushIds(new File("/data/comments/crawlerconf/163songID.txt"),"zy_comments_163");
        System.out.println(getSize("zy_comments_163"));


//        lpush("zy_comments_weibo","https://weibo.com/3167305545/HCkC4vkHR?type=comment");
        System.out.println(getSize("zy_comments_weibo"));

//        setValue("zy_comments_info","weibo_cookie","Cookie: YF-V5-G0=b1c63e15d8892cdaefd40245204f0e21; Ugrow-G0=cf25a00b541269674d0feadd72dce35f; WBtopGlobal_register_version=8c86b9ca67e1b502; _s_tentry=-; appkey=; Apache=9784520404509.354.1562564970498; SINAGLOBAL=9784520404509.354.1562564970498; ULV=1562564970506:1:1:1:9784520404509.354.1562564970498:; SUB=_2AkMqflZCf8NxqwJRmP0UxW_ib452wgnEieKcIqeZJRMxHRl-yT83qkE9tRB6Af54rXBv4PmLUwqPhNEWqpo92dHsIGLJ; SUBP=0033WrSXqPxfM72-Ws9jqgMF55529P9D9W5qPCArkAhC3k399T_1cPwe; login_sid_t=876677a26c4f6d825c62dc6e6dd70036; cross_origin_proto=SSL; WBStorage=988f187486ad9919|undefined; wb_view_log=1920*10801; YF-Page-G0=580fe01acc9791e17cca20c5fa377d00|1562565000|1562564988");
//        System.out.println(RedisUtils.getValue("zy_comments_info","weibo_cookie"));

    }
}
