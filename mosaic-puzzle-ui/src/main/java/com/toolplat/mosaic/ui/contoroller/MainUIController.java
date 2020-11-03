package com.toolplat.mosaic.ui.contoroller;

import com.google.common.collect.Lists;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.toolplat.mosaic.core.MosaicMaker;
import com.toolplat.mosaic.core.util.ImageUtil;
import com.toolplat.mosaic.ui.util.AlertUtil;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import okhttp3.Call;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MainUIController extends BaseFXController {

    @FXML
    private TextField sourceDir;

    @FXML
    private TextField aimPath;

    @FXML
    private ChoiceBox mode;

    @FXML
    private TextField targetW;

    @FXML
    private ScrollPane targetImgPane;

    @FXML
    private ImageView targetImg;

    @FXML
    private TextArea console;

    @FXML
    private ProgressBar readProcess;

    @FXML
    private ProgressBar writeProcess;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Button btnRun;

    @FXML
    private Label readme;

    @FXML
    private ImageView followImg;

    @FXML
    private ImageView sponsorImg;

    BufferedImage image = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        zoomSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(String.format("%1$.3f", zoomSlider.getValue()));
            Double val = zoomSlider.getValue() == 0 ? 0.1 : zoomSlider.getValue();
            targetImg.setFitWidth(targetImg.getImage().getWidth() * val / 100);
            targetImg.setFitHeight(targetImg.getImage().getHeight() * val / 100);

        });

        mode.getSelectionModel().selectFirst();

        // 使用说明
        readme.setText("马赛克拼图软件使用说明: \n " +
                "1. 选择图片目录，图片数量越多越好. \n" +
                "2. 选择需要的参照图片. \n " +
                "3. 设置生成的图片是灰色(GRAY)或者是彩色(RGB). \n" +
                "4. 点击开始运行. \n" +
                "5. 运行结束，保存图片");

        // 图片
        followImg.setImage(new ImageView("image/qrcode_weichat.jpg").getImage());
        sponsorImg.setImage( new ImageView("image/qrcode_alipay.jpg").getImage());
        String qrWeChat = "https://raw.githubusercontent.com/suyin58/mosaic-puzzle/main/mosaic-puzzle-ui/src/main/resources/image/qrcode_weichat.jpg";
        String qrAlipay = "https://raw.githubusercontent.com/suyin58/mosaic-puzzle/main/mosaic-puzzle-ui/src/main/resources/image/qrcode_alipay.jpg";
        Image imageWeChat = loadWebUrl(qrWeChat);
        Image imageAlipay = loadWebUrl(qrAlipay);
        if(null != imageWeChat){
            followImg.setImage(imageWeChat);
        }
        if(null != imageAlipay){
            sponsorImg.setImage(imageAlipay);
        }
    }

    @FXML
    public void chooseSourceDir(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedFolder = directoryChooser.showDialog(getPrimaryStage());
        if (selectedFolder != null) {
            sourceDir.setText(selectedFolder.getAbsolutePath());
        }
    }

    @FXML
    public void chooseAimPath(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("请选择图片文件", Lists.newArrayList("*.bmp",
                "*.png", "*.jpeg", "*.jpg")));

        File selectedFolder = fileChooser.showOpenDialog(getPrimaryStage());
        if (selectedFolder != null) {
            aimPath.setText(selectedFolder.getAbsolutePath());
        }
    }

    @FXML
    public void run(ActionEvent actionEvent) {

        clear();

        try {

            MosaicMaker mosaicMaker = new MosaicMaker(sourceDir.getText(), aimPath.getText());
            // 并发数
            final int nThreads = Runtime.getRuntime().availableProcessors();
            mosaicMaker.setThreadNum(nThreads);
            // 重复次数,暂时无用
            mosaicMaker.setMax(3);
            // 自动计算透明度
            mosaicMaker.setAutoBlend(false);

            // 生成样式
            mosaicMaker.setMode(mode.getValue().toString());

            // 设置目标图片大小(像素)
            mosaicMaker.setTargetW(Integer.parseInt(targetW.getText()));

            // 设置输出控制台
            mosaicMaker.setConsole(console);
            // 设置进度条
            mosaicMaker.setReadProcess(readProcess);
            mosaicMaker.setWriteProcess(writeProcess);

            new Thread(() -> {
                Platform.runLater(()->{
                    btnRun.setDisable(true);
                    console.setVisible(true);
                });
                try {
                    image = mosaicMaker.make();
                    targetImg.setImage(SwingFXUtils.toFXImage(image, null));
                    System.out.println("绘制结束1");
                    // targetImg.setFitHeight(targetImgPane.getWidth());
                    zoomSlider.setValue(targetImgPane.getWidth() / image.getWidth() * 100);
                    zoomSlider.setValueChanging(true);

                    System.out.println("绘制结束2");
                } catch (IOException e) {
                    e.printStackTrace();
                    AlertUtil.showErrorAlert(e.getMessage());
                }finally {
                    Platform.runLater(()-> {
                        btnRun.setDisable(false);
                        console.setVisible(false);
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert(e.getMessage());
            return;
        }

    }

    @FXML
    public void save(ActionEvent actionEvent) {

        if(null == image){
            AlertUtil.showErrorAlert("图片未生成");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        File selectedFolder = fileChooser.showSaveDialog(getPrimaryStage());
        if (selectedFolder != null) {
            String filePath = selectedFolder.getAbsolutePath();
            try {
                ImageUtil.save(image, filePath);
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showErrorAlert(e.getMessage());
                return;
            }
        }
    }


    private void clear() {
        Platform.runLater(() -> {
            targetImg.setImage(null);
            readProcess.setProgress(0);
            writeProcess.setProgress(0);
            zoomSlider.setValue(0);
            image = null;
        });
    }

    private Image loadWebUrl(String url) {
        OkHttpClient client =  new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.MILLISECONDS)
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .build();
        Request request = new Request.Builder()
//                .headers(getBaiduHeader())
                .get()
                .url(url)
                .build();

        Call call = client.newCall(request);
        //同步调用,返回Response,会抛出IO异常
        try {
            Response response = call.execute();
            JPEGImageDecoder decoderFile = JPEGCodec.createJPEGDecoder(response.body().byteStream());
            BufferedImage image = decoderFile.decodeAsBufferedImage();
            return SwingFXUtils.toFXImage(image, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
