package com.toolplat.mosaic.core;

import com.toolplat.mosaic.core.constant.Mode;

import java.io.IOException;

/**
 * 马赛克 拼图 main方法执行
 * @author suyin
 */
public class Main {

    public static void main(String[] args) {
        MosaicMaker mosaicMaker = new MosaicMaker("/Users/suyin/Documents/temp/avatar/"
                , "/Users/suyin/Documents/temp/source.png"
                , "/Users/suyin/Documents/temp/target_gray6.jpg");
        mosaicMaker.setMode(Mode.RGB);
        // 并发数
        final int nThreads = Runtime.getRuntime().availableProcessors();
        mosaicMaker.setThreadNum(nThreads);
        // 重复次数
        mosaicMaker.setMax(3);
        // 自动计算透明度
        mosaicMaker.setAutoBlend(false);
        // 设置目标图片大小
        mosaicMaker.setTargetH(4480*2);
        mosaicMaker.setTargetW(6720*2);
        try {
            mosaicMaker.make();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
