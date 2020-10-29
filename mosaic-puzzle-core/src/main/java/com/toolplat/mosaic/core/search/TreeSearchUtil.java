package com.toolplat.mosaic.core.search;

import com.toolplat.mosaic.core.domain.PuzzleUnit;
import com.toolplat.mosaic.core.util.ImageUtil;
import com.toolplat.mosaic.core.constant.Mode;
import com.toolplat.mosaic.core.util.PHashUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class TreeSearchUtil {
    public static BufferedImage getColseByGray(TreeMap<String, List<PuzzleUnit>> tree, String key) {

        Map.Entry<String,List<PuzzleUnit>> low = tree.floorEntry(key);
        Map.Entry<String,List<PuzzleUnit>> high = tree.ceilingEntry(key);
        Map.Entry<String,List<PuzzleUnit>>  select = null;
        if (low != null && high != null) {
            select =
                    Math.abs(Double.valueOf(key)-Double.valueOf(low.getKey())) < Math.abs(Double.valueOf(key)-Double.valueOf(high.getKey()))
                    ?   low
                    :   high;
        } else if (low != null || high != null) {
            select = low != null ? low : high;
        }
        return getSelectBufferedImage(tree, select.getKey(), select.getValue());
    }

    /**
     * 比较值0～1范围取之，越接近1，越相似
     * @param tree
     * @param key
     * @return
     */
    public static BufferedImage getColseByPHash(TreeMap<String, List<PuzzleUnit>> tree, String key) {

        if (tree == null || key == null) {
            return null;
        }
        String result = "";
        double min = 0;
        for (String k : tree.keySet()) {
            double d = PHashUtil.calculateSimilarity(k, key);
            if (d > min) {
                min = d;
                result = k;
            }
        }
        List<PuzzleUnit> list = tree.get(result);
        return getSelectBufferedImage(tree, result, list);

    }


    /**
     * R、G、B 三色分别绝对值求和，取平均数
     * @param tree
     * @param key
     * @return
     */
    public static BufferedImage getColseByRGB(TreeMap<String, List<PuzzleUnit>> tree, String key) {
        if (tree == null || key == null) {
            return null;
        }

        float min = 900.f;
        String[] rgb = key.split("-");
        float r = Float.parseFloat(rgb[0]);
        float g = Float.parseFloat(rgb[1]);
        float b = Float.parseFloat(rgb[2]);
        String result = "";
        for (String k : tree.keySet()) {
            String[] mrgb = k.split("-");
            float mr = Float.parseFloat(mrgb[0]);
            float mg = Float.parseFloat(mrgb[1]);
            float mb = Float.parseFloat(mrgb[2]);
            float curDif = Math.abs(r - mr) + Math.abs(g - mg) + Math.abs(b - mb);
            if (min > curDif) {
                min = curDif;
                result = k;
            }
        }
        List<PuzzleUnit> list = tree.get(result);
        return getSelectBufferedImage(tree, result, list);
    }

    private static BufferedImage getSelectBufferedImage(TreeMap<String, List<PuzzleUnit>> tree, String key,
                                                        List<PuzzleUnit> value) {
        Collections.shuffle(value);
        Optional<PuzzleUnit> optional = value.stream().filter(it -> it.max > 0).findFirst();
        try {
            if (optional.isPresent()) {
                optional.get().max--;

                return ImageUtil.resize(ImageIO.read(new File(optional.get().filePath)), optional.get().width,
                        optional.get().height);
            } else {
                // 移除图片
//            tree.remove(key);
                return ImageUtil.resize(ImageIO.read(new File(value.get(0).filePath)), value.get(0).width,
                        value.get(0).height);
            }
        } catch (IOException e) {
            return null;
        }
    }


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

    //计算平均灰度
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
//                avgGray += (77 * r + 150 * g + 29 * b + 128);
                avgGray += (0.299 * r + 0.597 * g + 0.114 * b);
            }
        }
        return avgGray / (w * h);
    }

    //计算平均rgb
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

}
