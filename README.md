# X5Browser

## github地址 https://github.com/iamlocky/X5Browser
## 基于腾讯x5内核的精简版浏览器
## 适用于需要单独的页面显示web内容，官方webview不能满足需求的场景
# 
![enter image description here](https://github.com/iamlocky/X5Browser/blob/master/shots/Screenshot_2017-08-13-16-23-04-002_com.example.lo.png?raw=true)
# 
![enter image description here](https://github.com/iamlocky/X5Browser/blob/master/shots/Screenshot_2017-08-13-16-23-30-700_com.example.lo.png?raw=true)
# 
![enter image description here](https://github.com/iamlocky/X5Browser/blob/master/shots/Screenshot_2017-08-13-16-28-02-014_com.example.lo.png?raw=true)
# 

# 使用方法：
# 1.按要求修改项目https://x5.tencent.com/tbs/guide/sdkInit.html

# 然后
Add it in your root build.gradle at the end of repositories:

    allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
	
# 
Add the dependency

    dependencies {
	        compile 'com.github.iamlocky:X5Browser:1.0.1'
	}

# 2.复制manifests中的权限和三个activity标签（如果你的as开启了自动合并manifest就可以不需要加activity标签）

```
<!--权限复制开始-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.READ_SETTINGS" />
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.GET_TASKS" />
    <!--复制结束-->
```

```
<!--activity复制开始-->
        <activity
            android:name="lockyluo.x5corelibrary.FullScreenActivity"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:label="@string/app_name"></activity>
        <activity
            android:name="lockyluo.x5corelibrary.FilechooserActivity"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:label="@string/app_name"></activity>
        <activity
            android:name="lockyluo.x5corelibrary.BrowserActivity"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:label="@string/app_name"
            android:launchMode="standard"></activity>
        <!--复制结束-->
```

# 3.在你的app中调用BrowserActivity.init() 
# 
## **至少要传递（context，是否日间模式，主题色）三个参数**
# 例如

			{
            //带网址夜间
                BrowserActivity.init(this, "https://x5.tencent.com/tbs/guide/sdkInit.html", false, ResourcesCompat.getColor(getResources(), R.color.colorBlueDark, getTheme()));
            
            //不带网址白天
                BrowserActivity.init(this, true, ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));
             }


# -----

# 欢迎各位大佬fork回去完善
