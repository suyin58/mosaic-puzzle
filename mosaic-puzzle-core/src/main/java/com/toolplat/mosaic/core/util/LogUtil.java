package com.toolplat.mosaic.core.util;

/**
 * @author suyin
 */
public class LogUtil {

    /**
     * 百分比进度打印
     * @param msg
     * @param cur
     * @param sum
     */
    public static void logProcess(String msg, int cur, int sum) {
        if(cur % (sum / 100) == 0){
           log(msg +" " +(cur * 100.0 / sum) +"%");
        }
        if(cur == (sum -1)){
            log(msg +" 完成");
        }

    }

    public static void log(String msg){
        System.out.println(msg);
    }

}
