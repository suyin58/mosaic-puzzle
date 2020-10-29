package com.toolplat.mosaic;

import com.toolplat.mosaic.util.Mode;

import java.io.IOException;

/**
 * 方法执行
 * @author suyin
 */
public class Main {

    public static void main(String[] args) {
        MosaicMaker mosaicMaker = new MosaicMaker("/Users/suyin/Documents/temp/avatar/"
                , "/Users/suyin/Documents/temp/wedding.jpg"
                , "/Users/suyin/Documents/temp/target_rgb5.jpg");
        mosaicMaker.setMode(Mode.RGB);
        mosaicMaker.setMax(1);
        mosaicMaker.setDefaultH(6720*2);
        mosaicMaker.setDefaultW(4480*2);
        try {
            mosaicMaker.make();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
