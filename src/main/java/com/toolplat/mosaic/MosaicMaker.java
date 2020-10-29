package com.toolplat.mosaic;

import com.google.common.collect.Lists;
import com.toolplat.mosaic.crawler.ImageClear;
import com.toolplat.mosaic.domain.PuzzleUnit;
import com.toolplat.mosaic.search.TreeUtil;
import com.toolplat.mosaic.util.ImageUtil;
import com.toolplat.mosaic.util.Mode;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MosaicMaker {
    //图库路径
    private String dbPath;
    //目标图片路径
    private String aimPath;
    //图片输出路径
    private String outPath;
    //默认子图宽
    private int subWidth = 64;
    //默认子图高
    private int subHeight = 64;
    //粒度
    private int unitMin;
    //成像方式
    private String mode;
    //默认生成图宽
    private int defaultW;
    //默认生成图高
    private int defaultH;
    //每张素材最多出现的次数
    private int max;
    //加载图库使用的线程数
    private int threadNum;
    private TreeMap<String,List<PuzzleUnit>> tree = new TreeMap();

    public MosaicMaker(String dbPath, String aimPath, String outPath) {
        this(dbPath, aimPath, outPath, 64, 64, 5, Mode.RGB, 1920, 1080, 300, 4);
    }

    public MosaicMaker(String dbPath, String aimPath, String outPath, int subWidth, int subHeight, int unitMin, String mode, int defaultW, int defaultH, int max, int threadNum) {
        this.dbPath = dbPath;
        this.aimPath = aimPath;
        this.outPath = outPath;
        this.subWidth = subWidth;
        this.subHeight = subHeight;
        this.unitMin = unitMin;
        this.mode = mode;
        this.defaultW = defaultW;
        this.defaultH = defaultH;
        this.max = max;
        this.threadNum = threadNum;
    }

    public String getDBPath() {
        return dbPath;
    }

    public void setDBPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getAimPath() {
        return aimPath;
    }

    public void setAimPath(String aimPath) {
        this.aimPath = aimPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public int getSubWidth() {
        return subWidth;
    }

    public void setSubWidth(int subWidth) {
        this.subWidth = subWidth;
    }

    public int getSubHeight() {
        return subHeight;
    }

    public void setSubHeight(int subHeight) {
        this.subHeight = subHeight;
    }

    public int getUnitMin() {
        return unitMin;
    }

    public void setUnitMin(int unitMin) {
        this.unitMin = unitMin;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getDefaultW() {
        return defaultW;
    }

    public void setDefaultW(int defaultW) {
        this.defaultW = defaultW;
    }

    public int getDefaultH() {
        return defaultH;
    }

    public void setDefaultH(int defaultH) {
        this.defaultH = defaultH;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }


    public void make() throws IOException {
        File aimFile = new File(aimPath);
        BufferedImage aimIm = ImageIO.read(aimFile);
        //使用默认尺寸
        aimIm = ImageUtil.resize(aimIm, defaultW, defaultH);
        int aimWidth = aimIm.getWidth();
        int aimHeight = aimIm.getHeight();
        // 计算单元大小
        calSubIm(aimWidth, aimHeight);
        // 删除重复图片
        ImageClear.clearByMd5(dbPath);
        readAllImage();
        core(aimIm);
    }

    private void core(BufferedImage aimIm) throws IOException {
        int width = aimIm.getWidth();
        int height = aimIm.getHeight();
        BufferedImage newIm = new BufferedImage(width, height, aimIm.getType());
        Graphics2D g = newIm.createGraphics();
        int w = width / subWidth;
        System.out.println("拼图共需要" + (w * w) + "张图片，目前读取"+readImg.get()+"张,重复率"+((w * w) / readImg.get()));
        long start = System.currentTimeMillis();
        ExecutorService pool = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(w);
        for (int i = 0; i < w; i++) {
            int finalI = i;
            pool.execute(() -> {
                for (int j = 0; j < w; j++) {
//                    System.out.printf("正在拼第%d张图片\n", (finalI + 1) * (j + 1));
                    int x = finalI * subWidth;
                    int y = j * subHeight;
                    BufferedImage curAimSubIm = aimIm.getSubimage(x, y, subWidth, subHeight);
                    BufferedImage fitSubIm = findFitIm(curAimSubIm);
                    g.drawImage(fitSubIm, x, y, subWidth, subHeight, null);
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("计数器抛异常:");
        } finally {
            pool.shutdown();
        }
        System.out.println("拼图完成，耗时" + (System.currentTimeMillis() - start) + "毫秒");
        ImageUtil.save(newIm, outPath);
    }

    //搜索合适子图
    public BufferedImage findFitIm(BufferedImage image) {
        switch (mode) {
            case Mode.RGB:
                return TreeUtil.getColseByRGB(tree,TreeUtil.calKey(image, mode));
            case Mode.GRAY:
                return TreeUtil.getColseByGray(tree,TreeUtil.calKey(image, mode));
            default:
                return null;
        }
    }


    private AtomicInteger readImg = new AtomicInteger(1);
    //读取图库
    public void readAllImage() {
        File dir = new File(this.dbPath);
        File[] files = dir.listFiles();
        long start = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(threadNum);
        int size = files.length;
        ReadTask[] readTask = new ReadTask[threadNum];
        CountDownLatch latch = new CountDownLatch(threadNum);
        for (int i = 0; i < size; i++) {
            if (files[i].isFile()) {
                int index = i % threadNum;
                if (readTask[index] == null) {
                    readTask[index] = new ReadTask(latch, subWidth, subHeight);
                }
                readTask[index].add(files[i]);
            }
        }
        System.out.println("开始加载图库");
        for (int i = 0; i < threadNum; i++) {
            pool.execute(readTask[i]);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
        System.out.println("共读取" + readImg.get() + "张图片");

        System.out.println("读取图库完成，耗时" + (System.currentTimeMillis() - start) + "毫秒");
    }

    private class ReadTask implements Runnable {

        private CountDownLatch latch;
        private List<File> files = new ArrayList<>();
        private int w;
        private int h;

        public ReadTask(CountDownLatch latch, int w, int h) {
            this.latch = latch;
            this.w = w;
            this.h = h;
        }

        public void add(File file) {
            files.add(file);
        }

        @Override
        public void run() {
            for (File f : files) {
                if (f.isFile()) {
                    PuzzleUnit unit = null;
                    try {

                        BufferedImage bi  = ImageUtil.resize(ImageIO.read(f), w, h);
                        System.out.println("正在读取文件" + readImg.incrementAndGet());
                        String key = TreeUtil.calKey(bi, mode);
                        unit =  new PuzzleUnit(max,mode,key,bi);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(null == unit){
                        continue;
                    }
                    if(!tree.containsKey(unit.key)){
                        tree.put(unit.key, Lists.newArrayList(unit));
                    }else{
                        tree.get(unit.key).add(unit);
                    }

                }
            }
            latch.countDown();
        }
    }

    /**
     * 计算子团尺寸
     */
    private void calSubIm(int w, int h) {
        int size = 50;
        subWidth = size;
        double d  = size * (h / (w * 1.0));
        subHeight = (int)d ;
    }

}
