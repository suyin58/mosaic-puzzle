package com.toolplat.mosaic.core.util;

import com.google.common.collect.Lists;
import com.toolplat.mosaic.core.constant.Mode;
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
        if(alpha == 0){
            return source;
        }
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


    /**
     * 计算image的灰度方差，并根据方差提供一个0～30的透明度建议
     *
     * @param image
     * @return
     */
    public static final int calcBlend(BufferedImage image) {
        double calcStandardDeviation = calcStandardDeviation(image);
        if(calcStandardDeviation > 100){
            return 30;
        }
        if(calcStandardDeviation < 30){
            return 0;
        }
        return ((int)calcStandardDeviation - 30) * 2/7;
    }


    /**
     * 计算key值，用于存放treeMap中便于搜索
     * @param image
     * @param mode
     * @return
     */
    public static final String calKey(BufferedImage image, String mode) {
        switch (mode) {
            case Mode.GRAY:
                return "" + calAvgGRAY(image);
            case Mode.RGB:
                float[] res = calAvgRGB(image);
                return res[0] + "-" + res[1] + "-" + res[2];
            case Mode.PHASH:
                return PHashUtil.getFeatureValue(image);
            default:
                return "";
        }
    }

    /**
     * 平均灰度
     * @param image
     * @return
     */
    private static double calAvgGRAY(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        double avgGray = 0.f;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = image.getRGB(x, y);
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                avgGray += (19595 * r + 38469 * g + 7472 * b) >> 16;
            }
        }
        return avgGray / (w * h);
    }

    /**
     * 计算平均rgb
     * @param image
     * @return
     */
    private static float[] calAvgRGB(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        float[] res = new float[3];
        float avgR = 0.f;
        float avgG = 0.f;
        float avgB = 0.f;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = image.getRGB(x, y);
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                avgR += r;
                avgG += g;
                avgB += b;
            }
        }
        res[0] = avgR / (w * h);
        res[1] = avgG / (w * h);
        res[2] = avgB / (w * h);
        return res;
    }


    /**
     * 传入一个图片，计算标准差,r/g/b的标准差平均数
     * 正太分布
     * @param image
     * @return
     */
    public static double calcStandardDeviation(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        double[] r = new double[w * h];
        double[] g = new double[w * h];
        double[] b = new double[w * h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = image.getRGB(x, y);
                r[h * x + y] = (pixel & 0xff0000) >> 16;
                g[h * x + y] = (pixel & 0xff00) >> 8;
                b[h * x + y] = (pixel & 0xff);

            }
        }
        double sr = MathUtil.standardDeviation(r);
        double sb = MathUtil.standardDeviation(b);
        double sg = MathUtil.standardDeviation(g);


        return (sr + sb + sg) / 3;
    }

    public static final BufferedImage resize(BufferedImage im, int w, int h) throws IOException {
        return Thumbnails.fromImages(Lists.newArrayList(im)).size(w, h).keepAspectRatio(false).asBufferedImage();
    }

}
