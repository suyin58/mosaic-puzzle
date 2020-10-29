package com.toolplat.mosaic.core.domain;

import com.toolplat.mosaic.core.constant.Mode;


/**
 * @author suyin
 */
public class PuzzleUnit implements Comparable<PuzzleUnit> {
    /**
     * 最大重复数量
     */
    public int max;
    /**
     * rgb/gray
     */
    public String mode;
    public String key;


    public String filePath;
    public int height;
    public int width;


    public PuzzleUnit(int max, String key, String filePath, int height, int width) {
        this.max = max;
        this.key = key;
        this.filePath = filePath;
        this.height = height;
        this.width = width;
    }

    //返回1表示当前值大于比较值，返回-1表示当前值小于比较值,返回0表示相等
    @Override
    public int compareTo(PuzzleUnit o) {
        switch (o.mode) {
            case Mode.RGB:
                float dif = calDif(o);
                float door = 78.0f;
                if (dif > door) {
                    return 1;
                } else if (dif < door) {
                    return -1;
                } else {
                    return 0;
                }
            case Mode.GRAY:
                Float cur = Float.parseFloat(this.key);
                Float cmp = Float.parseFloat(o.key);
                if (cur > cmp) {
                    return 1;
                } else if (cur < cmp) {
                    return -1;
                } else {
                    return 0;
                }
            case Mode.PHASH:
                //d表示汉明距离
                float d = calDif(o);
                if (d > 5) {
                    return 1;
                } else if (d < 5) {
                    return -1;
                } else {
                    return 0;
                }
            default:
                return 0;
        }
    }

    private float calDif(PuzzleUnit o) {
        switch (mode) {
            case Mode.RGB:
                String[] curKeys = this.key.split("-");
                float r = Float.parseFloat(curKeys[0]);
                float g = Float.parseFloat(curKeys[1]);
                float b = Float.parseFloat(curKeys[2]);
                String[] mk = o.key.split("-");
                float mr = Float.parseFloat(mk[0]);
                float mg = Float.parseFloat(mk[1]);
                float mb = Float.parseFloat(mk[2]);
                return (Math.abs(mr - r) + Math.abs(mg - g) + Math.abs(mb - b));
            case Mode.GRAY:
                return Math.abs(Float.parseFloat(this.key) - Float.parseFloat(o.key));
            case Mode.PHASH:
                int length = this.key.length();
                int d = 0;
                for (int i = 0; i < length; i++) {
                    if (this.key.charAt(i) != o.key.charAt(i)){
                        d++;
                    }
                }
                return d;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                "key=" + key +
                '}';
    }
}
