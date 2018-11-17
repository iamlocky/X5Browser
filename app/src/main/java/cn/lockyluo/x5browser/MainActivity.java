package cn.lockyluo.x5browser;

import android.Manifest;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lockyluo.x5browser.R;
import com.lockyluo.x5corelibrary.BrowserActivity;
import com.lockyluo.x5corelibrary.utils.FileUtils;
import com.lockyluo.x5corelibrary.utils.ToastUtil;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

import java.io.File;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    TextView textView1;
    TextView textView2;
    TextView textViewT;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        }

        textViewT = findViewById(R.id.tv_tips);
        textViewT.setText("使用方法\n" +
                "1.按要求修改项目https://x5.tencent.com/tbs/guide/sdkInit.html\n" +
                "2.复制manifests中的权限和三个activity标签\n" +
                "3.在你的app中调用BrowserActivity.init()\n" +
                "\n虽然BrowserActivity会动态申请sd权限，为避免不可预测的bug最好先动态申请manifest中需要的权限再启动");

        textView1 = findViewById(R.id.tv_hello);
        textView1.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorBlueDark, getTheme()));

        textView2 = findViewById(R.id.tv_hello2);
        textView2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {

            }

            @Override
            public void onInstallFinish(int i) {

            }

            @Override
            public void onDownloadProgress(int i) {
                ToastUtil.show(getApplicationContext(),"tbs下载进度 "+i+"%");
            }
        });
    }


    public void onClick(View v) {//demo

        switch (v.getId()) {
            case R.id.tv_hello://带网址夜间
                BrowserActivity.init(this, "http://baidu.com/", false, ResourcesCompat.getColor(getResources(), R.color.colorBlueDark, getTheme()));
                break;

            case R.id.tv_hello2://不带网址白天
                BrowserActivity.init(this, true, ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));
                break;
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void btn1Click(View view) {
//        File tbsPath=new File(ContextCompat.getDataDir(getApplicationContext()).getAbsolutePath() + "/app_tbs/core_private");
        File tbsPath=new File(Environment.getExternalStorageDirectory()+"/tencent/tbs/"+getPackageName());
        if (!tbsPath.exists()){
            tbsPath.mkdirs();
        }
//        FileUtils.copyAssetAndWrite(getApplicationContext(),"tbslock.txt",tbsPath.getAbsolutePath());
        FileUtils.copyAssetAndWrite(getApplicationContext(),"x5.tbs","tbs.apk",tbsPath.getAbsolutePath());
        Log.d(TAG, "btn1Click: " + Environment.getExternalStorageDirectory());
        Log.d(TAG, "btn1Click: " + ContextCompat.getDataDir(getApplicationContext()).getAbsolutePath());
//        try {
//            FileUtils.copy(Environment.getExternalStorageDirectory().getAbsolutePath() + "/core_share",
//                    ContextCompat.getDataDir(getApplicationContext()).getAbsolutePath() + "/app_tbs/core_share");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void btn2Click(View view) {
        Log.d(TAG, "btn1Click: " + ContextCompat.getDataDir(getApplicationContext()));
    }

    public void btn3Click(View view) {
        Log.d(TAG, "btn1Click: " + Environment.getExternalStorageDirectory()+"/tencent/tbs/"+getPackageName());
    }


    public void btn4Click(View view) {
    }

}
