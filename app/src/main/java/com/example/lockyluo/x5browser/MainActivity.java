package com.example.lockyluo.x5browser;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.x5corelibrary.BrowserActivity;
import com.example.x5corelibrary.utils.ToastUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }


    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tv_hello:
                BrowserActivity.init(this,true, ResourcesCompat.getColor(getResources(),R.color.colorPrimary,null));
                break;
            case R.id.tv_hello2:
                BrowserActivity.init(this,"cy-ber.cn",false, ResourcesCompat.getColor(getResources(),R.color.colorPrimary,null));

                break;
            default:
                break;
        }

    }
}
