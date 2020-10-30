package com.toolplat.mosaic.core.util;

import com.google.common.collect.Lists;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {

    public static final void save(BufferedImage image, String outPath) throws IOException {
        File outFile = new File(outPath);
        ImageIO.write(image, "JPEG", outFile);
    }


    /**
     * 透明度
     * @param source 需要做透明的图
     * @param ref 参照图
     * @param alpha 0～100
     * @return
     */
    public static final BufferedImage blend(BufferedImage source, BufferedImage ref, int alpha) {
        int width = source.getWidth();
        int height = source.getHeight();
        //尺寸不一样则返回空
        if (width != ref.getWidth() || height != ref.getHeight()) {
            return null;
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel1 = source.getRGB(x, y);
                int pixel2 = ref.getRGB(x, y);
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
                source.setRGB(x, y, rgb);
            }
        }
        return source;
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
