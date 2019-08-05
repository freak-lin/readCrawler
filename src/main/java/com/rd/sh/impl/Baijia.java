package com.rd.sh.impl;



import com.rd.sh.utils.GoodHTTP;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Baijia {
    private static String[] pageurls = {"https://baijiahao.baidu.com/u?app_id=1591970815766489&fr=bjharticle"};
    private static String[] headers = {"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
            "Cookie:123123;"};
    private static List<User> users = new ArrayList<>();
    private static final int TIME = 7;
    private static int number= 0;

    public static void main(String[] args) {
        System.out.println(getReadCount("https://baijiahao.baidu.com/u?app_id=1632580605576781"));
    }
    //获取是否是原创或者大v
    public static String[] getStatus(String pageurl){
        String dv = "0";
        String original = "0";
        String data = "";
            try {
                data = GoodHTTP.sendGet(pageurl, headers, "utf-8", 3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        //data.contains("\"company\":\"优质原创作者\""
            if (data.contains("\"company\":\"\\u4f18\\u8d28\\u539f\\u521b\\u4f5c\\u8005\"")) {
                original = "1";
            }else {
                original = "0";
            }
            //data.contains("我是百+计划上榜作者,等你PK!")
            if (data.contains("\"promote_title\":\"\\u6211\\u662f\\u767e+\\u8ba1\\u5212\\u4e0a\\u699c\\u4f5c\\u8005,\\u7b49\\u4f60PK!\"")) {
                dv = "1";
            } else {
                dv = "0";
            }
        return new String[]{dv, original};
    }
    public static int getReadCount(String pageurl) {
        number = 0;
        String query = "";
            String uk = getUKfromURL(pageurl);
            if (uk != null) {
                try {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String url = "https://mbd.baidu.com/webpage?tab=article&num=6&uk=" + uk + "&type=newhome&action=dynamic&format=jsonp";
                    String data = GoodHTTP.sendGet(url, headers,"utf-8",3000).replace("callback(", "");
                    data = data.substring(0, data.length() - 1);
                    JSONObject jsonObject = JSONObject.fromObject(data);
                    JSONArray userJson = jsonObject.getJSONObject("data").getJSONArray("list");
                    for (int i = 0; i < userJson.size(); i++) {
                        JSONObject object2 = (JSONObject) userJson.get(i);
                        JSONObject object = object2.getJSONObject("asyncParams");
                        User user = (User) JSONObject.toBean(object, User.class);
                        user.setTitle(object2.getJSONObject("itemData").getString("title"));
                        user.setUrl(object2.getJSONObject("itemData").getString("url"));
                        users.add(user);
                        //如果是七天前数据就停止
//                        if (Long.parseLong(object2.getJSONObject("itemData").getString("created_at")) < getPastDate(TIME)) {
//                            query = "0";
//                            break;
//                        }
                    }
                    //获取分页阅读数量
                    if (!query.equals("0")) {
                        try {
                            JSONObject jsonObject1 = (JSONObject) jsonObject.getJSONObject("data").get("query");
                            query = jsonObject1.getString("ctime");
                        } catch (Exception e) {
                            System.out.println("没有下一页");
                        }
                    }
                    while (!query.isEmpty() && !query.equals("0")) {
                        query = getPageRead(uk, query);
                    }
                    //获取阅读数
                    return getReadCountFromUsers(users);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        return number;
    }

    public static String getUKfromURL(String url) {
        String page = "";
        try {
            page = GoodHTTP.sendGet(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document doc = Jsoup.parse(page);
        Elements scriptElements = doc.getElementsByTag("script");
        for (Element ele : scriptElements) {
            //检查是否有uk变量
            String script = ele.data();
            if (script.contains("uk")) {
                try {
                    script = script.split("\n")[1].replace("window.runtime = ","");
                    script = script.substring(0, script.length() - 1);
                    JSONObject jsonObject = JSONObject.fromObject(script);
                    String uk = jsonObject.getJSONObject("user").getString("uk");
                    System.out.println(uk);
                    return uk;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static int getReadCountFromUsers(List<User> users) {
        int count = 0;
        for (User user : users) {
            try {
                String param = "";
                JSONObject jsonObject = JSONObject.fromObject(user);
                try {
                    System.out.println(jsonObject.toString());
                    param = URLEncoder.encode(jsonObject.toString(), "utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String url = "https://mbd.baidu.com/webpage?type=homepage&action=interact&format=jsonp&params=[" + param + "]&uk=f1TmWrReh3qcgDKhyYN0qw";
                String data = GoodHTTP.sendGet(url, headers,"utf-8",3000).replace("callback(", "");
                data = data.substring(0, data.length() - 1);
                //获取数据
                int index = data.indexOf("\"read_num\"");
                String countString = data.substring(index, data.length());
                int index2 = countString.indexOf(",");
                count += Integer.valueOf(countString.substring(11, index2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        users.clear();
        return count;
    }

    public static String getPageRead(String uk, String ctime) {
        String query = "";
        String url = "https://mbd.baidu.com/webpage?tab=article&num=10&uk=" + uk + "&ctime=" + ctime + "&type=newhome&action=dynamic&format=jsonp";
        String data = "";
        try {
            data = GoodHTTP.sendGet(url, headers, "utf-8", 3000).replace("callback(", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        data = data.substring(0, data.length() - 1);
        JSONObject jsonObject = JSONObject.fromObject(data);
        JSONArray userJson = jsonObject.getJSONObject("data").getJSONArray("list");
        for (int i = 0; i < userJson.size(); i++) {
            JSONObject object2 = (JSONObject) userJson.get(i);
            JSONObject object = object2.getJSONObject("asyncParams");
            User user = (User) JSONObject.toBean(object, User.class);
            user.setTitle(object2.getJSONObject("itemData").getString("title"));
            user.setUrl(object2.getJSONObject("itemData").getString("url"));
            users.add(user);
            long datatime = Long.parseLong(object2.getJSONObject("itemData").getString("created_at"));
//            if (datatime < getPastDate(TIME)) {
//                query = "0";
//                break;
//            }
        }
        try {
            if (!query.equals("0")) {
                JSONObject jsonObject1 = (JSONObject) jsonObject.getJSONObject("data").get("query");
                query = jsonObject1.getString("ctime");
            }
        } catch (Exception e) {
            System.out.println("没有下一页");
        }
        return query;
    }
    public static long getPastDate(int past) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime()/ 1000;
    }

    public static class User {
        private String user_type;
        private String dynamic_id;
        private String feed_id;
        private String dynamic_type;
        private String dynamic_sub_type;
        private String thread_id;
        private String title;
        private String url;


        @Override
        public String toString() {
            return "User{" +
                    "user_type='" + user_type + '\'' +
                    ", dynamic_id='" + dynamic_id + '\'' +
                    ", feed_id='" + feed_id + '\'' +
                    ", dynamic_type='" + dynamic_type + '\'' +
                    ", dynamic_sub_type='" + dynamic_sub_type + '\'' +
                    ", thread_id='" + thread_id + '\'' +
                    '}';
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDynamic_sub_type() {
            return dynamic_sub_type;
        }

        public void setDynamic_sub_type(String dynamic_sub_type) {
            this.dynamic_sub_type = dynamic_sub_type;
        }

        public String getUser_type() {
            return user_type;
        }

        public void setUser_type(String user_type) {
            this.user_type = user_type;
        }

        public String getDynamic_id() {
            return dynamic_id;
        }

        public void setDynamic_id(String dynamic_id) {
            this.dynamic_id = dynamic_id;
        }

        public String getFeed_id() {
            return feed_id;
        }

        public void setFeed_id(String feed_id) {
            this.feed_id = feed_id;
        }

        public String getDynamic_type() {
            return dynamic_type;
        }

        public void setDynamic_type(String dynamic_type) {
            this.dynamic_type = dynamic_type;
        }

        public String getThread_id() {
            return thread_id;
        }

        public void setThread_id(String thread_id) {
            this.thread_id = thread_id;
        }
    }
}
