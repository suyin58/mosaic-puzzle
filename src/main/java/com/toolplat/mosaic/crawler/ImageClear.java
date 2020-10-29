package com.toolplat.mosaic.crawler;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

/**
 * 爬虫可能爬取了较多删除MD5相同的图片
 * @author suyin
 */
public class ImageClear {

    static final String PARH="/Users/suyin/Documents/temp/avatar";

    public static void main(String[] args) throws IOException {
        clearByMd5(PARH);
    }

    public static void clearByMd5(String path) throws IOException {
        File fPath = new File(path);
        File[] subFiles = fPath.listFiles();
        System.out.println("文件数量 -> "+subFiles.length);
        Set<String> md5s = new HashSet<>();
        for (File f : subFiles){
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                String md5 = DigestUtils.md5Hex(fis);
                if (md5s.contains(md5)) {
                    f.delete();
                    continue;
                }
                md5s.add(md5);
            }finally {
                if(null != fis){
                    fis.close();
                }
            }
        }

        File fPath1 = new File(path);
        File[] subFiles1 = fPath1.listFiles();
        System.out.println("文件数量 -> "+subFiles1.length);
    }


}
