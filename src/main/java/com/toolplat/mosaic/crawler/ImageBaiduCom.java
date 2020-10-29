package com.toolplat.mosaic.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 从百度下载图片测试使用
 */
public class ImageBaiduCom {

    private static final String targetPath = "/Users/suyin/Documents/temp/avatar/";
    private static final Integer RN = 30;
    public static void main(String[] args) throws IOException, InterruptedException {
        String url = "https://image.baidu.com/search/acjson?tn=resultjson_com&logid=10763507436069158249&ipn=rj&ct=201326592&is=&fp=result&queryWord=%E5%89%A7%E7%85%A7%E7%90%89%E7%92%83&cl=2&lm=-1&ie=utf-8&oe=utf-8&adpicid=&st=-1&z=&ic=&hd=&latest=&copyright=&word=%E5%89%A7%E7%85%A7%E7%90%89%E7%92%83&s=&se=&tab=&width=&height=&face=0&istype=2&qc=&nc=1&fr=&expermode=&force=&pn=30&rn=30&gsm=1e&1603971148293=";
        String gsm = url.substring(url.indexOf("gsm=") + "gsm=".length()
                , url.indexOf("gsm=") + "gsm=".length() + 2);
        int n = 1;
        while(gsm != null && n < 100){
            Thread.sleep(100);

            gsm = requestUrl(url,gsm, n);
            n++;
        }
    }

    private static String requestUrl(String url, String gsm, int n) throws IOException {
        url = getGsmUrl(url, gsm, n);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
//                .headers(getBaiduHeader())
                .get()
                .url(url)
                .build();

        Call call = client.newCall(request);
        //同步调用,返回Response,会抛出IO异常
        Response response = call.execute();
        String string = new BufferedReader(new InputStreamReader(response.body().byteStream()))
                .lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject jsonObject =  JSON.parseObject(string);
        JSONArray data = jsonObject.getJSONArray("data");
        if(data.size() < 3){
            System.out.println("无法获取到更多图片，当前页数:" + n);
            System.exit(0);
        }

        for (int i = 0 ; i< data.size(); i++){

            JSONObject item =  data.getJSONObject(i);
            downloadImage(item.getString("middleURL"));
        }

        return jsonObject.getString("gsm");
    }

    /**
     * 更换url--> 分页
     * @param url
     * @param gsm
     * @param n
     * @return
     */
    private static String getGsmUrl(String url, String gsm, int n) {

        // 替换 gsm
        String result = replaceParam(url, "gsm", gsm);
        // 替换 pn
        result = replaceParam(result, "pn", ""+n * 30);
        return result;
    }

    /**
     * 替换url中的参数
     * @param url
     * @param paramName
     * @param val
     * @return
     */
    public static String replaceParam(String url, String paramName, String val) {
        return url.replaceAll("(" + paramName +"=[^&]*)", paramName + "=" + val);
    }

    private static Headers getBaiduHeader() {
        Map<String, String> map = new HashMap(10);
        map.put("Accept","text/plain, */*; q=0.01");
        map.put("Accept-Encoding","gzip, deflate, br");
        map.put("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8");
        map.put("Cache-Control","no-cache");
        map.put("Connection","keep-alive");
        map.put("Cookie","BDIMGISLOGIN=0; winWH=%5E6_1680x836; BDqhfp=%E5%A4%B4%E5%83%8F%26%2600-10-1undefined%26%26554%26%262; BIDUPSID=4B16CDE325172198AD992CDFBA08A964; PSTM=1585806524; BAIDUID=628C98C1C46FE643B77DC29784C53D1F:FG=1; BDUSS=EhIU0FpTlRicWF0Y3NqMlVEdlB4S1NCR0xkcHJrR28xWERoaVZGTEd5S3JBaGxmSVFBQUFBJCQAAAAAAAAAAAEAAAA1LwsF1eahpGRvdGHO3surAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKt18V6rdfFeM; cleanHistoryStatus=0; BDUSS_BFESS=EhIU0FpTlRicWF0Y3NqMlVEdlB4S1NCR0xkcHJrR28xWERoaVZGTEd5S3JBaGxmSVFBQUFBJCQAAAAAAAAAAAEAAAA1LwsF1eahpGRvdGHO3surAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKt18V6rdfFeM; BDSFRCVID=oE0OJeC62ldsOr5rVnihjPnn-e2w995TH6aow6MxFwHubXHCecnkEG0P_f8g0KubNp3eogKKKgOTHICF_2uxOjjg8UtVJeC6EG0Ptf8g0M5; H_BDCLCKID_SF=tb4q_ItaJC-3HPozq45B5-Ljh2T22jnZ-259aJ5nJDoCe4I6jnjK-f0QyM6T2tCt-nrqQM5FQpP-HqTgLT7mh4-Ehn-JQpc8ygnpKl0MLUOlbb0xyTOD5pIsyxnMBMni52OnapTn3fAKftnOM46JehL3346-35543bRTLnLy5KJtMDF4jj8BDj5LDNRf2bjWHjv-B4nVajraqjrnhPF306_zXP6-35KH056-KxQF-jC5OP3F0-T_XqFvMfnB2h37JD6yL45454c_O-cajxkaDUPF2-oxJpOgQRbMopvaKDjrSJ6vbURvyP-g3-7A3M5dtjTO2bc_5KnlfMQ_bf--QfbQ0hOhqP-j5JIEoKtatIKbhC-r5nJbqRFqMqrt2D62aKDs0nccBhcqEIL4jTrW-x-XKmcUX63BLIoX24t-btOlOxbSj4Qo3Rk_Dn5laJbp3mJiKCOC0l5nhMJbb67JDMn3-ltLX4oy523iab3vQpnzEpQ3DRoWXPIqbN7P-p5Z5mAqKl0MLPbtbb0xb6_0D65BDHA8t6Ksb5vfsJbVaJ3MHJRcq4bohjPhbtb9BtQmJJrfh4okKJOKKq_GX46mh4CIbM6RJx6ZQg-q3RAaypcdjUbRWRotQp-Z04De0x-jLIQOVn0MW-5DeMb-X4nJyUPUbPnnBUcm3H8HL4nv2JcJbM5m3x6qLTKkQN3T-PKO5bRu_CF-JK-ahC8RDTRb5nbH5hoea4IXKKOLVb7Ntp7keq8CD4vfKU4Xjq5p2tc32HRXWj6Htt0BbD52y5jHhnbBXq7l-MvtKHb75b5JaJvpsIJMBPDWbT8U5f5wbTJ-aKviahvjBMb1OqODBT5h2M4qMxtOLR3pWDTm_q5TtUJMeCnTDMFhe4tX-NFqJjKJJM5; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; MCITY=-179%3A; PSINO=5; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0; ZD_ENTRY=baidu; delPer=1; H_PS_PSSID=32819_1455_32843_32931_31254_32705_32230_7516_7605_32117_32911; BA_HECTOR=a504842l81218hf03i1fpi7ap0k; BDRCVFR[X_XKQks0S63]=mk3SLVN4HKm; userFrom=null; firstShowTip=1; BDRCVFR[dG2JNJb_ajR]=mk3SLVN4HKm; BDRCVFR[-pGxjrCMryR]=mk3SLVN4HKm; indexPageSugList=%5B%22%E5%A4%B4%E5%83%8F%22%2C%22%E7%94%B5%E5%BD%B1%E5%89%A7%E7%85%A7%22%2C%22%E7%94%B5%E5%BD%B1%22%2C%22%E5%B1%B1%E6%B2%B3%22%2C%22%E9%A3%8E%E6%99%AF%22%2C%22%E8%A1%A8%E6%83%85%E5%8C%85%22%2C%22%E6%9E%B6%E6%9E%84%E5%9B%BE%22%2C%22%E5%85%B3%E6%B3%A8%20icon%22%5D");
        map.put("Host","image.baidu.com");
        map.put("Pragma","no-cache");
        map.put("Referer","https://image.baidu.com/search/index?tn=baiduimage&ipn=r&ct=201326592&cl=2&lm=-1&st=-1&fm=result&fr=&sf=1&fmq=1603870554797_R&pv=&ic=0&nc=1&z=0&hd=0&latest=0&copyright=0&se=1&showtab=0&fb=0&width=&height=&face=0&istype=2&ie=utf-8&sid=&word=%E5%A4%B4%E5%83%8F");
        map.put("Sec-Fetch-Dest","empty");
        map.put("Sec-Fetch-Mode","cors");
        map.put("Sec-Fetch-Site","same-origin");
        map.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
        map.put("X-Requested-With","XMLHttpRequest");
        return Headers.of(map);
    }

    private static void downloadImage(String url) {
        if(null == url || url.length() == 0){
            return;
        }
        System.out.println("down load image:" + url);
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("requestFail:" + url);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //拿到字节流
                InputStream is = response.body().byteStream();

                int len = 0;
                File file  = new File(targetPath, UUID.randomUUID().toString() +".png");
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1){
                    fos.write(buf, 0, len);
                }

                fos.flush();
                //关闭流
                fos.close();
                is.close();
            }
        });
    }
}
