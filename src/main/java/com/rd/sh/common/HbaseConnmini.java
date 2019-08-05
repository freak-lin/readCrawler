package com.rd.sh.common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;

import java.io.IOException;
import java.util.ArrayList;

public class HbaseConnmini {

    private static Configuration hcfg = HBaseConfiguration.create();
    static LoadConf lcnf = null;
    static ArrayList<HConnection> listHconn = new ArrayList<HConnection>();
    //新版本

    public static String HBASE_VISITED;
    public static String OUR_SOURCE;
    public static String REDIS_HOST;
    public static int REDIS_PORT;
    public static String START_TIME;
    public static String END_TIME;

    static {
        try {
            lcnf = new LoadConf();
            HBASE_VISITED = lcnf.getProperty("hbase_visited_table");
            OUR_SOURCE = lcnf.getProperty("our_source");
            REDIS_HOST = lcnf.getProperty("redis_host");
            REDIS_PORT = Integer.parseInt(lcnf.getProperty("redis_port"));
            START_TIME = lcnf.getProperty("start_time");
            END_TIME = lcnf.getProperty("end_time");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public HbaseConnmini() {
    }

}
