package com.rd.sh.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 内存中动态添加log4j2配置文件实现动态指定日志文件
 */
public class LogUtils {

    static String rootPath = "";
    static {
            rootPath = "/data/dongfanghao/logs/";
//        rootPath ="C:\\Users\\admin\\Desktop"
    }

    static Map<String, Logger> mapChache = new HashMap<>();

    /**
     * 加锁的日志获取方法
     * @param logName
     * @return
     */
    public static synchronized Logger getLogger(String logName){
        if(mapChache.containsKey(logName)){
            return mapChache.get(logName);
        }
        Logger logger = initLogger(logName);
        mapChache.put(logName,logger);
        return logger;
    }





    private static Logger initLogger(String logName) {
        //为false时，返回多个LoggerContext对象
//        LoggerContext ctx = (LoggerContext) LogManager.getContext(true);
//        LoggerContext ctx = LoggerContext.getContext();
        //新建名为dynamic上下文配置环境
        LoggerContext ctx =new LoggerContext("dynamic");
        Configuration config = ctx.getConfiguration();
        Layout layout = PatternLayout.createDefaultLayout();

        //创建filter
        ThresholdFilter[] filtersArr= new ThresholdFilter[2];
        filtersArr[0] =  ThresholdFilter.createFilter(Level.WARN, Filter.Result.DENY, Filter.Result.NEUTRAL);
        filtersArr[1] =  ThresholdFilter.createFilter(Level.INFO, Filter.Result.ACCEPT, Filter.Result.DENY);
        CompositeFilter filters = CompositeFilter.createFilters(filtersArr);
        // 创建RollingFileAppender
        Appender appender = RollingFileAppender.createAppender(rootPath+logName+".log",rootPath+logName+"%d{yyyy-MM-dd}.log", "true", logName,
                "flase" , null, "true", TimeBasedTriggeringPolicy.createPolicy("1","true"),null,layout,filters,  null, null, null, config);

        // 获取控制台Appender
        ConsoleAppender consoleAppender = config.getAppender("Console");

        //配置文件添加appender
        config.addAppender(appender);
        config.addAppender(consoleAppender);
        AppenderRef ref = AppenderRef.createAppenderRef("" + logName, null, null);
        AppenderRef[] refs = new AppenderRef[]{ref};

        //创建AsyncLogger
        LoggerConfig loggerConfig = AsyncLoggerConfig.createLogger("false", Level.ALL, "" + logName,
                "true", refs, null, config, null);

        //引用appender
        loggerConfig.addAppender(appender, null, null);
        loggerConfig.addAppender(consoleAppender,null,null);

        //配置文件添加AsyncLogger
        config.addLogger(logName, loggerConfig);
        ctx.updateLoggers();
        return ctx.getLogger(logName);
    }


    public static void writeLog(String commentContent){
        Logger logger = LogUtils.getLogger("log_");
        logger.info("/n"+commentContent);

    }


    public static void main(String[] args) {
        getLogger("liupan").info("刘攀");
        getLogger("liupan").info("刘攀");
        getLogger("liupan").info("刘攀");
//        System.out.println(Long.MAX_VALUE - 1545165000000302750L);
    }
}
