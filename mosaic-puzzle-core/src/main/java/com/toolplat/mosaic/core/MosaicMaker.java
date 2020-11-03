package com.toolplat.mosaic.core;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.toolplat.mosaic.core.constant.Mode;
import com.toolplat.mosaic.core.domain.PuzzleUnit;
import com.toolplat.mosaic.core.search.TreeSearchUtil;
import com.toolplat.mosaic.core.util.ImageUtil;
import com.toolplat.mosaic.core.util.LogUtil;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 马赛克 拼图
 */
public class MosaicMaker {
    /**
     * 图库路径
     */
    private String sourceDir;
    /**
     * 目标图片路径
     */
    private String targetFile;
    /**
     * 默认子图宽
     */
    private int unitW = 80;
    /**
     * 默认子图高
     */
    private int unitH = 60;
    /**
     * 成像方式
     */
    private String mode;
    /**
     * 默认生成图宽
     */
    private int targetW;
    /**
     * 默认生成图高
     */
    private int targetH;
    /**
     * 每张素材最多出现的次数 ，目前暂未使用
     */
    private int max;

    /**
     * 自动计算落到每张图片自动计算透明度 -- 根据图片的平均差异度
     */
    private boolean autoBlend;

    /**
     * 加载图库使用的线程数
     */
    private int threadNum;


    /**
     * 读取进度条
     */
    private ProgressIndicator readProcess;

    /**
     * 绘制进度条
     */
    private ProgressIndicator writeProcess;

    /**
     * 控制台
     */
    private TextArea console;

    /**
     * 数据容器
     */
    private TreeMap<String,List<PuzzleUnit>> tree = new TreeMap();

    /**
     * 计数器
     */
    private AtomicInteger counter = new AtomicInteger(1);

    public MosaicMaker(String sourceDir, String targetFile) {

        this(sourceDir, targetFile, 64, 64, Mode.RGB, 1920, 0, 300, 4);
    }

    public MosaicMaker(String sourceDir, String targetFile, int unitW, int unitH, String mode, int targetW, int targetH, int max, int threadNum) {
        this.sourceDir = sourceDir;
        this.targetFile = targetFile;
        this.unitW = unitW;
        this.unitH = unitH;
        this.mode = mode;
        this.targetW = targetW;
        this.targetH = targetH;
        this.max = max;
        this.threadNum = threadNum;
    }

    public String getDBPath() {
        return sourceDir;
    }

    public void setDBPath(String dbPath) {
        this.sourceDir = dbPath;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public int getUnitW() {
        return unitW;
    }

    public void setUnitW(int unitW) {
        this.unitW = unitW;
    }

    public int getUnitH() {
        return unitH;
    }

    public void setUnitH(int unitH) {
        this.unitH = unitH;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getTargetW() {
        return targetW;
    }

    public void setTargetW(int targetW) {
        this.targetW = targetW;
    }

    public int getTargetH() {
        return targetH;
    }

    public void setTargetH(int targetH) {
        this.targetH = targetH;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isAutoBlend() {
        return autoBlend;
    }

    public void setAutoBlend(boolean autoBlend) {
        this.autoBlend = autoBlend;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public TextArea getConsole() {
        return console;
    }

    public void setConsole(TextArea console) {
        LogUtil.console = console;
        this.console = console;
    }

    public ProgressIndicator getReadProcess() {
        return readProcess;
    }

    public void setReadProcess(ProgressIndicator readProcess) {
        this.readProcess = readProcess;
    }

    public ProgressIndicator getWriteProcess() {
        return writeProcess;
    }

    public void setWriteProcess(ProgressIndicator writeProcess) {
        this.writeProcess = writeProcess;
    }


    /**
     * 入口方法
     * @return
     * @throws IOException
     */
    public BufferedImage make() throws IOException {
        Stopwatch watch = Stopwatch.createStarted();
        // 关键参数校验
        checkParam();

        File aimFile = new File(targetFile);
        BufferedImage aimIm = ImageIO.read(aimFile);

        //使用默认尺寸
        if(targetH == 0){
            targetH = targetW * aimIm.getHeight() / aimIm.getWidth();
        }
        try {
            aimIm = ImageUtil.resize(aimIm, targetW, targetH);
        } catch (Exception e){
            LogUtil.log("异常：", e);
            throw e;
        }
        int aimWidth = aimIm.getWidth();
        int aimHeight = aimIm.getHeight();
        // 计算单元大小
        calSubIm(aimWidth, aimHeight);

        int width = aimIm.getWidth();
        int height = aimIm.getHeight();
        int w = width / unitW;
        int h = height / unitH;
        LogUtil.log("开始读取图片");
        loadImageFromSourceDir();
        LogUtil.log("读取图库完成共读取" + counter.get() + "张图片，耗时" + watch.elapsed(TimeUnit.SECONDS) + "秒");
        LogUtil.log("拼图开始共需要" + (w * h) + "张图片，目前读取"+ counter.get()+"张,预计重复率"+((w * 1.0 * h) / counter.get()));
        BufferedImage result = drawNewImage(aimIm);
        LogUtil.log("拼图完成，耗时" + watch.elapsed(TimeUnit.SECONDS) + "秒");
        return result;
    }

    private void checkParam() {
        File dbDir = new File(sourceDir);
        if(!dbDir.exists() || !dbDir.isDirectory()){
            LogUtil.log("图片目录错误:" + sourceDir);
            throw new RuntimeException("图片目录错误");
        }

        File aimFile = new File(targetFile);

        if(!aimFile.exists() || ! aimFile.isFile()){
            LogUtil.log("参照图片错误:" + sourceDir);
            throw new RuntimeException("参照图片错误");
        }

        if(targetW == 0){
            throw new IllegalArgumentException("生成的图片宽度未设置");
        }
    }

    /**
     * 绘制图片
     * @param aimIm
     * @return
     * @throws IOException
     */
    private BufferedImage drawNewImage(BufferedImage aimIm) throws IOException {
        int width = aimIm.getWidth();
        int height = aimIm.getHeight();
        int w = width / unitW;
        int h = height / unitH;

        BufferedImage newIm = new BufferedImage(width, height, aimIm.getType());
        Graphics2D g = newIm.createGraphics();
        counter = new AtomicInteger(0);
        ExecutorService pool = Executors.newFixedThreadPool(threadNum);
        List<Future> fList = new ArrayList<>();
        for (int i = 0; i < w; i++) {
            int finalI = i;
            for (int j = 0; j < h; j++) {
                int finalJ = j;
                Future f = pool.submit(() -> {
                    int x = finalI * unitW;
                    int y = finalJ * unitH;
                    BufferedImage curAimSubIm = aimIm.getSubimage(x, y, unitW, unitH);
                    BufferedImage fitSubIm = findBestUnit(this.mode, curAimSubIm);
                    if (autoBlend) {
                        fitSubIm = ImageUtil.blend(fitSubIm, curAimSubIm, ImageUtil.calcBlend(fitSubIm));
                    }
                    int n = counter.incrementAndGet();
                    LogUtil.logProcess("图片绘制中……", n, w * h);
                    LogUtil.showProcess(writeProcess, n, w * h);
                    g.drawImage(fitSubIm, x, y, unitW, unitH, null);
                });
                fList.add(f);
            }
        }
        for (Future f : fList) {
            try {
                f.get();
            } catch (Exception e) {
                LogUtil.log("计数器抛异常:", e);
            }
        }
        pool.shutdown();
        return newIm;
    }


    /**
     * 搜索图片
     * @param mode
     * @param image
     * @return
     */
    private BufferedImage findBestUnit(String mode, BufferedImage image) {
        switch (mode) {
            case Mode.RGB:
                return TreeSearchUtil.getColseByRGB(tree, ImageUtil.calKey(image, mode));
            case Mode.GRAY:
                return TreeSearchUtil.getColseByGray(tree, ImageUtil.calKey(image, mode));
            case Mode.PHASH:
                return TreeSearchUtil.getColseByPHash(tree, ImageUtil.calKey(image, mode));
            default:
                return null;
        }
    }


    /**
     * 读取图片
     */
    public void loadImageFromSourceDir() {
        File dir = new File(this.sourceDir);
        File[] files = dir.listFiles();
        ExecutorService pool = Executors.newFixedThreadPool(threadNum);
        List<Future> fList = new ArrayList<>();
        for (File file : files) {
            Future f = pool.submit(() -> {
                if (file.isFile()) {
                    PuzzleUnit unit = null;
                    try {
                        BufferedImage bi = ImageUtil.resize(ImageIO.read(file), unitW, unitH);
                        String key = ImageUtil.calKey(bi, mode);
                        bi = null;
                        int n = counter.incrementAndGet();
                        LogUtil.logProcess("读取文件中……", n, files.length);
                        LogUtil.showProcess(readProcess, n, files.length);
                        unit = new PuzzleUnit(max, key, file.getAbsolutePath(), unitW, unitH);
                    } catch (Exception e) {
                        LogUtil.log("图片读取异常，", e);
                    }
                    if (null != unit) {
                        if (!tree.containsKey(unit.key)) {
                            tree.put(unit.key, Lists.newArrayList(unit));
                        } else {
                            tree.get(unit.key).add(unit);
                        }
                    }
                }
            });
            fList.add(f);
        }
        for (Future f : fList) {
            try {
                f.get();
            } catch (Exception e) {
                LogUtil.log("", e);
            }
        }
        pool.shutdown();
    }


    /**
     * 计算子团尺寸
     */
    private void calSubIm(int w, int h) {
        unitW = w / 200;
        if(unitW > 80){
            unitW = 80;
        }
        unitH = unitW * 3/4 ;
    }

}
