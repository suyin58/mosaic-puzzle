package com.toolplat.mosaic.core.util;

import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;


/**
 * @author suyin
 */
public class LogUtil {

    public static TextArea console = null;

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

    /**
     * 百分比进度展现
     * @param progress
     * @param cur
     * @param sum
     */
    public static void showProcess(ProgressIndicator progress, int cur, int sum) {
        if(null == progress){
            return;
        }
        Platform.runLater(()->{

            if(cur % (sum / 100) == 0){
                progress.setProgress((cur * 1.0  / sum));
            }
            if(cur == (sum -1)){
                progress.setProgress(1);
            }
        });
    }



    public static void log(String msg){
        System.out.println(msg);
        if(null != console){
            Platform.runLater(()->{
                console.setText(console.getText() + msg +"\n");
                console.setScrollTop(Double.MAX_VALUE);
            });
        }
    }

}
