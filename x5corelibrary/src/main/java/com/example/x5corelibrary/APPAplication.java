package com.example.x5corelibrary;

import android.app.Application;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.example.x5corelibrary.utils.ToastUtil;
import com.tencent.smtt.sdk.QbSdk;

public class APPAplication extends MultiDexApplication {
	private static APPAplication instance;
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		//搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
		QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
			
			@Override
			public void onViewInitFinished(boolean arg0) {
				//x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
				Log.d("app", " onViewInitFinished is " + arg0);
			}
			
			@Override
			public void onCoreInitFinished() {
			}
		};
		//x5内核初始化接口
		try {
			QbSdk.initX5Environment(getApplicationContext(),  cb);
		} catch (Exception e) {
			e.printStackTrace();
			ToastUtil.show(getApplicationContext(),"x5内核启动失败");
		}
	}

	public static APPAplication getInstance(){
		return instance;
	}

}
