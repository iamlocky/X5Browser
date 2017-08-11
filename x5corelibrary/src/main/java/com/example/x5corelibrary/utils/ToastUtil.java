package com.example.x5corelibrary.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by lockyluo on 2017/8/10.
 */

public class ToastUtil {
    /**
     * 可覆盖前一条消息显示的toast工具
     */
    private static Toast mToast;//控制toast时间

    public static void show(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
        Log.i("lockyToast", "--------------------------------");
        Log.i("lockyToast", text);
    }
}
