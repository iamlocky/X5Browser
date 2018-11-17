package com.lockyluo.x5corelibrary.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.lockyluo.x5corelibrary.APPAplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


/**
 * Created by LockyLuo on 2017/11/4.
 * 功能：用线程保存图片
 */

public class SaveImage extends AsyncTask<String, Void, String> {
    private String imgurl = "";

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        try {
            imgurl = params[0];
            if (imgurl == null)
                return null;
            String sdcard = Environment.getExternalStorageDirectory().toString();
            File file = new File(sdcard + "/Download");
            if (!file.exists()) {
                file.mkdirs();
            }

            BufferedInputStream inputStream = null;
            URL url = new URL(imgurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(20000);
            String type="image/jpg";
            if (conn.getResponseCode() == 200) {
                inputStream = new BufferedInputStream(conn.getInputStream());
                type=HttpURLConnection.guessContentTypeFromStream(inputStream);
            }
            type=type==null?"image/jpg":type;
            int idx = type.lastIndexOf("/");
            String ext = type.substring(idx+1);
            file = new File(sdcard + "/Download/" + new Date().getTime()  +"."+ext);
            byte[] buffer = new byte[4096];
            int len = 0;
            FileOutputStream outStream = new FileOutputStream(file);
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);

            }

            outStream.close();
            result = "图片已保存至：" + file.getAbsolutePath();
        } catch (Exception e) {
            result = "保存失败！" + e.getLocalizedMessage();
        }
        return result;
    }

    @Override
    protected void onPostExecute(final String result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(APPAplication.getInstance(),result,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
