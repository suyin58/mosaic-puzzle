package com.toolplat.mosaic.util;

import com.google.common.collect.Lists;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {

    public static final void save(BufferedImage image, String outPath) throws IOException {
        File outFile = new File(outPath);
        ImageIO.write(image, "JPEG", outFile);
    }



    //透明
    public static final BufferedImage blend(BufferedImage im1, BufferedImage im2, float a) {
        int width = im1.getWidth();
        int height = im1.getHeight();
        //尺寸不一样则返回空
        if (width != im2.getWidth() || height != im2.getHeight()) {
            return null;
        }
        int alpha = (int) (a * 100);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel1 = im1.getRGB(x, y);
                int pixel2 = im2.getRGB(x, y);
                int r1 = (pixel1 & 0xff0000) >> 16;
                int g1 = (pixel1 & 0xff00) >> 8;
                int b1 = (pixel1 & 0xff);
                int r2 = (pixel2 & 0xff0000) >> 16;
                int g2 = (pixel2 & 0xff00) >> 8;
                int b2 = (pixel2 & 0xff);
                int r = ((100 - alpha) * r1 + alpha * r2) / 100;
                int g = ((100 - alpha) * g1 + alpha * g2) / 100;
                int b = ((100 - alpha) * b1 + alpha * b2) / 100;
                int rgb = Integer.valueOf(Integer.toHexString(r & 0xff) + Integer.toHexString(g & 0xff) + Integer.toHexString(b & 0xff), 16);
                im1.setRGB(x, y, rgb);
            }
        }
        return im1;
    }

    public static final BufferedImage resize(BufferedImage im, int w, int h) {

        try {
            return Thumbnails.fromImages(Lists.newArrayList(im)).size(w,h).keepAspectRatio(false).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
