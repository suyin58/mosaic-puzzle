package com.toolplat.mosaic.util;

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
        if(cur % (sum / 20) == 0){
            System.out.println(msg +" " +(cur * 100.0 / sum) +"%");
        }
        if(cur == (sum -1)){
            System.out.println(msg +" 完成");
        }

    }

}
