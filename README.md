# X5Browser
## 基于腾讯x5内核的精简版浏览器
## 适用于需要单独的页面显示web内容，官方webview不能满足需求的场景
# 
![enter image description here](https://github.com/iamlocky/X5Browser/blob/master/shots/Screenshot_2017-08-13-16-23-04-002_com.example.lo.png?raw=true)
# 
![enter image description here](https://github.com/iamlocky/X5Browser/blob/master/shots/Screenshot_2017-08-13-16-23-30-700_com.example.lo.png?raw=true)
# 
![enter image description here](https://github.com/iamlocky/X5Browser/blob/master/shots/Screenshot_2017-08-13-16-28-02-014_com.example.lo.png?raw=true)
# 
# 
# 
# 
#使用方法：
# 1.按要求修改项目https://x5.tencent.com/tbs/guide/sdkInit.html

# 然后
Add it in your root build.gradle at the end of repositories:

    allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
Add the dependency

    dependencies {
	        compile 'com.github.iamlocky:X5Browser:1.0.1'
	}

#2.复制manifests中的权限和三个activity标签
#3.在你的app中调用BrowserActivity.init() 
## **至少要传递（context，是否日间模式，主题色）三个参数**
# 例如

			{
            //带网址夜间
                BrowserActivity.init(this, "https://x5.tencent.com/tbs/guide/sdkInit.html", false, ResourcesCompat.getColor(getResources(), R.color.colorBlueDark, getTheme()));
            
            //不带网址白天
                BrowserActivity.init(this, true, ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));
             }


