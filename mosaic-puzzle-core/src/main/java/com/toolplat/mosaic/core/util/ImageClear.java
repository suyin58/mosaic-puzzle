package com.toolplat.mosaic.core.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 爬虫可能爬取了较多删除MD5相同的图片
 * @author suyin
 */
public class ImageClear {


    public static void main(String[] args) throws IOException {
        String PARH="/Users/suyin/Documents/temp/avatar";
        File dbFile = new File(PARH);
        File[] dbFiles = dbFile.listFiles();
        clearByMd5(dbFiles);
    }

    public static void clearByMd5(File[] dbFiles) throws IOException {
        System.out.println("开始去重图片");
        Set<String> md5s = new HashSet<>();

        int delN = 0;
        for (int i = 0 ; i < dbFiles.length ; i++ ){
            File f = dbFiles[i];
            LogUtil.logProcess("图片去重中……", i , dbFiles.length);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                String md5 = DigestUtils.md5Hex(fis);
                if (md5s.contains(md5)) {
                    f.delete();
                    delN ++;
                    continue;
                }
                md5s.add(md5);
            }finally {
                if(null != fis){
                    fis.close();
                }
            }
        }
        System.out.println("去重完成，剩余图片" + (dbFiles.length - delN) + "张");
    }


}
