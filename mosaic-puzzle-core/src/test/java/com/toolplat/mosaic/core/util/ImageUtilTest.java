package com.toolplat.mosaic.core.util;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ImageUtilTest {

    @Test
    public void testWhiteGray(){
        int r = 0;
        int g = 0;
        int b = 0;

        int avgGray = (19595 * r + 38469 * g + 7472 * b) >> 16;
        System.out.println(avgGray);
    }

    @Test
    public void testBlackGray(){
        int r = 255;
        int g = 255;
        int b = 255;
        int avgGray = (19595 * r + 38469 * g + 7472 * b) >> 16;
        System.out.println(avgGray);
    }

    @Test
    public void testCalcStandardDeviation(){
        File path = new File("/Users/suyin/Documents/temp/standard");
        TreeMap<String, Double> tree = new TreeMap<>();
        for(File  f : path.listFiles()){
            try {
                BufferedImage bi = ImageIO.read(f);
                tree.put(f.getName(), ImageUtil.calcStandardDeviation(bi));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<String,Double> entry :  tree.entrySet()){

            System.out.println( entry.getValue()+"\t --> " +  entry.getKey());
        }

    }
}
