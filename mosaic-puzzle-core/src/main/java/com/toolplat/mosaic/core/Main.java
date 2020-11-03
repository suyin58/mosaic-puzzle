package com.toolplat.mosaic.core;

import com.toolplat.mosaic.core.constant.Mode;
import com.toolplat.mosaic.core.util.ImageUtil;

import java.io.IOException;

/**
 * 马赛克 拼图 main方法执行
 * @author suyin
 */
public class Main {

    public static void main(String[] args) {

        String outPath = "/Users/suyin/Documents/temp/target_gray7.jpg";

        MosaicMaker mosaicMaker = new MosaicMaker("/Users/suyin/Documents/temp/avatar/"
                , "/Users/suyin/Documents/temp/source.png");
        mosaicMaker.setMode(Mode.GRAY);
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

            ImageUtil.save(mosaicMaker.make(), outPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
