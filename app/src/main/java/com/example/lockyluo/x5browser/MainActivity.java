package com.example.lockyluo.x5browser;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.x5corelibrary.BrowserActivity;

public class MainActivity extends AppCompatActivity {
    TextView textView1;
    TextView textView2;
    TextView textViewT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewT = (TextView) findViewById(R.id.tv_tips);
        textViewT.setText("使用方法\n" +
                "1.按要求修改项目https://x5.tencent.com/tbs/guide/sdkInit.html\n" +
                "2.复制manifests中的权限和三个activity标签\n" +
                "3.在你的app中调用BrowserActivity.init()\n" +
                "");

        textView1 = (TextView) findViewById(R.id.tv_hello);
        textView1.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorBlueDark, getTheme()));

        textView2 = (TextView) findViewById(R.id.tv_hello2);
        textView2.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));


    }


    public void onClick(View v) {//demo

        switch (v.getId()) {
            case R.id.tv_hello://带网址夜间
                BrowserActivity.init(this, "cy-ber.cn", false, ResourcesCompat.getColor(getResources(), R.color.colorBlueDark, getTheme()));
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
}
