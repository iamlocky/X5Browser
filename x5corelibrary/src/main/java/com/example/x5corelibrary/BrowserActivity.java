package com.example.x5corelibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import com.example.x5corelibrary.utils.ToastUtil;
import com.example.x5corelibrary.utils.X5WebView;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient.CustomViewCallback;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.utils.TbsLog;

import java.net.MalformedURLException;
import java.net.URL;

public class BrowserActivity extends Activity {
    /**
     * 作为一个浏览器的示例展示出来，采用android+web的模式
     */
    public static X5WebView mWebView;
    private Context context;
    private ViewGroup mViewParent;
    private ImageButton mBack;
    private ImageButton mForward;
    private ImageButton mExit;
    private ImageButton mHome;
    private ImageButton mMore;
    private Button mMode;
    private Button mGo;
    private LinearLayout mToolbar;
    private EditText mUrl;
    private String currentUrl;
    private static String mHomeUrl = "http://m.baidu.com";
    private static final String TAG = "X5Sdk";
    private static final int MAX_LENGTH = 18;
    private static final int pressColor = 0x4C000000;
    private static int normalColor = 0xECF0F2;
    private boolean mNeedTestPage = false;

    private final int disable = 120;
    private final int enable = 255;
    private ProgressBar mPageLoadingProgressBar = null;

    private ValueCallback<Uri> uploadFile;

    private URL mIntentUrl;

    private static boolean isDayMode = true;
    public static int colorPrimary = 0;

    public static void init(Activity context, boolean isDayMode, int defaultColorPrimary) {//不带url启动

        if (isHexNumber(defaultColorPrimary + "")) {
            setColorPrimary(defaultColorPrimary);
        }
        setDayMode(isDayMode);
        context.startActivity(new Intent(context, BrowserActivity.class));
    }

    public static void init(Activity context, String url, boolean isDayMode, int defaultColorPrimary) {//带url启动
        if (url != null) {
            try {
                if(url.indexOf("http")==-1){
                    url="http://"+url;
                }
                mHomeUrl = (new URL(url).toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                mHomeUrl="http://m.baidu.com";
            }
        }
        init(context, isDayMode, defaultColorPrimary);
    }

    private static void setDayMode(boolean dayMode) {
        if (!isDayMode) {
            Log.d("night mode", "夜间模式状态改变，无法修改");
            return;
        }
        isDayMode = dayMode;
    }

    private static void setColorPrimary(int defaultColorPrimary) {
        colorPrimary = defaultColorPrimary;
    }


    //判断十六进制
    private static boolean isHexNumber(String str) {
        boolean flag = false;
        for (int i = 0; i < str.length(); i++) {
            char cc = str.charAt(i);
            if (cc == '0' || cc == '1' || cc == '2' || cc == '3' || cc == '4' || cc == '5' || cc == '6' || cc == '7' || cc == '8' || cc == '9' || cc == 'A' || cc == 'B' || cc == 'C' ||
                    cc == 'D' || cc == 'E' || cc == 'F' || cc == 'a' || cc == 'b' || cc == 'c' || cc == 'c' || cc == 'd' || cc == 'e' || cc == 'f') {
                flag = true;
            }
        }
        return flag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        context = BrowserActivity.this;
        try {//检查颜色是否为透明
            if (colorPrimary == 0) {
                setColorPrimary(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                normalColor = ResourcesCompat.getColor(getResources(), R.color.day, null);
            } else {
                mGo.setBackgroundColor(colorPrimary);
            }

        } catch (Exception e) {
            e.printStackTrace();
            setColorPrimary(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
            normalColor = ResourcesCompat.getColor(getResources(), R.color.day, null);
        }

        Intent intent = getIntent();
        if (intent != null) {
            try {
                mIntentUrl = new URL(intent.getData().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            } catch (Exception e) {
            }
        }
        //
        try {
            if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 11) {
                getWindow()
                        .setFlags(
                                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        } catch (Exception e) {
        }

		/*
         * getWindow().addFlags(
		 * android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 */
        setContentView(R.layout.activity_browser);
        mViewParent = (ViewGroup) findViewById(R.id.webView1);

        initBtnListenser();

        mTestHandler.sendEmptyMessageDelayed(MSG_INIT_UI, 10);

    }


    private void changGoForwardButton(WebView view) {
        if (view.canGoBack())
            mBack.setAlpha(enable);
        else
            mBack.setAlpha(disable);
        if (view.canGoForward())
            mForward.setAlpha(enable);
        else
            mForward.setAlpha(disable);
        if (view.getUrl() != null && view.getUrl().equalsIgnoreCase(mHomeUrl)) {
            mHome.setAlpha(disable);
            mHome.setEnabled(false);
        } else {
            mHome.setAlpha(enable);
            mHome.setEnabled(true);
        }
    }

    private void initProgressBar() {
        mPageLoadingProgressBar = (ProgressBar) findViewById(R.id.progressBar1);// new
        // ProgressBar(getApplicationContext(),
        // null,
        // android.R.attr.progressBarStyleHorizontal);
//		mPageLoadingProgressBar.setMax(100);
//		mPageLoadingProgressBar.setProgressDrawable(this.getResources()
//				.getDrawable(R.drawable.color_progressbar));
    }

    private void init() {

        mWebView = new X5WebView(this, null);

        mViewParent.addView(mWebView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        initProgressBar();

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mUrl.setText(url);
                return false;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // mTestHandler.sendEmptyMessage(MSG_OPEN_TEST_URL);
//                mTestHandler.sendEmptyMessageDelayed(MSG_OPEN_TEST_URL, 5000);// 5s?
                if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 16)
                    changGoForwardButton(view);

                String title = mWebView.getTitle();
                if (title != null && title.length() > MAX_LENGTH) {
                    mUrl.setText(title.subSequence(0, MAX_LENGTH) + "...");
                } else {
                    mUrl.setText(title);
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsConfirm(WebView arg0, String arg1, String arg2,
                                       JsResult arg3) {
                return super.onJsConfirm(arg0, arg1, arg2, arg3);
            }

            View myVideoView;
            View myNormalView;
            CustomViewCallback callback;

            // /////////////////////////////////////////////////////////
            //

            /**
             * 全屏播放配置
             */
            @Override
            public void onShowCustomView(View view,
                                         CustomViewCallback customViewCallback) {
                FrameLayout normalView = (FrameLayout) findViewById(R.id.web_filechooser);
                ViewGroup viewGroup = (ViewGroup) normalView.getParent();
                viewGroup.removeView(normalView);
                viewGroup.addView(view);
                myVideoView = view;
                myNormalView = normalView;
                callback = customViewCallback;
            }

            @Override
            public void onHideCustomView() {
                if (callback != null) {
                    callback.onCustomViewHidden();
                    callback = null;
                }
                if (myVideoView != null) {
                    ViewGroup viewGroup = (ViewGroup) myVideoView.getParent();
                    viewGroup.removeView(myVideoView);
                    viewGroup.addView(myNormalView);
                }
            }

            @Override
            public void onProgressChanged(WebView webView, int i) {
                super.onProgressChanged(webView, i);
                mPageLoadingProgressBar.setProgress(i);
            }

            @Override
            public boolean onJsAlert(WebView arg0, String arg1, String arg2,
                                     JsResult arg3) {
                /**
                 * 这里写入你自定义的window alert
                 */
                return super.onJsAlert(null, arg1, arg2, arg3);
            }
        });

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                    if (mGo.getVisibility() != View.GONE) {//--------隐藏go按钮
                        mGo.setVisibility(View.GONE);
                        String title = mWebView.getTitle();
                        if (title != null && title.length() > MAX_LENGTH)
                            mUrl.setText(title.subSequence(0, MAX_LENGTH) + "...");
                        else
                            mUrl.setText(title);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        mWebView.requestFocus();
                    }
                    return false;


            }
        });
        mWebView.setDayOrNight(isDayMode);

//        mWebView.setDownloadListener(new DownloadListener() {
//            @Override
//            public void onDownloadStart(String arg0, String s1, String s2, String s3, long l) {
//                {//部分x5内核无效
//                    TbsLog.d(TAG, "url: " + arg0);
//                    new AlertDialog.Builder(BrowserActivity.this)
//                            .setTitle("是否下载？")
//                            .setMessage(arg0)
//                            .setPositiveButton("立即下载",
//                                    (dialog, which) ->
//                                    {
//                                        Uri uri = Uri.parse(arg0);
//                                        Intent downloadIntent = new Intent(Intent.ACTION_VIEW, uri);
//                                        startActivity(downloadIntent);
//
//                                        ToastUtil.show(
//                                                context,
//                                                "开始下载");
//                                    })
//                            .setNegativeButton("取消",
//                                    (dialog, which) ->
//                                    {
//                                        ;
//                                        ToastUtil.show(
//                                                context,
//                                                "取消下载");
//                                    })
//                            .setOnCancelListener(
//                                    dialog ->
//                                    {
//                                        //
//                                    }).show();
//                }
//            }
//        });

        WebSettings webSetting = mWebView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);
        long time = System.currentTimeMillis();
        if (mIntentUrl == null) {
            mWebView.loadUrl(mHomeUrl);
        } else {
            mWebView.loadUrl(mIntentUrl.toString());
        }
        TbsLog.d("time-cost", "cost time: "
                + (System.currentTimeMillis() - time));
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().sync();


    }

    private void initBtnListenser() {
        mBack = (ImageButton) findViewById(R.id.btnBack1);
        mForward = (ImageButton) findViewById(R.id.btnForward1);
        mExit = (ImageButton) findViewById(R.id.btnExit1);
        mHome = (ImageButton) findViewById(R.id.btnHome1);
        mGo = (Button) findViewById(R.id.btnGo1);
        mUrl = (EditText) findViewById(R.id.editUrl1);
        mMore = (ImageButton) findViewById(R.id.btnMore);
        mMode = (Button) findViewById(R.id.btnDayOrNight);
        mToolbar = (LinearLayout) findViewById(R.id.toolbar1);

        if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 16) {
            mBack.setAlpha(disable);
            mForward.setAlpha(disable);
            mHome.setAlpha(disable);
        }
        mHome.setEnabled(false);

        mMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    if (isDayMode) {
                        isDayMode = false;//--------------夜间模式
                        mBack.setBackgroundColor(colorPrimary);
                        mForward.setBackgroundColor(colorPrimary);
                        mExit.setBackgroundColor(colorPrimary);
                        mHome.setBackgroundColor(colorPrimary);
                        mMore.setBackgroundColor(colorPrimary);
                        mToolbar.setBackgroundColor(colorPrimary);
                        mMode.setText("日");
                        normalColor = colorPrimary;
                    } else {
                        isDayMode = true;
                        mBack.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                        mForward.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                        mExit.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                        mHome.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                        mMore.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                        mToolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                        mMode.setText("夜");
                        normalColor = ResourcesCompat.getColor(getResources(), R.color.day, null);
                    }
                    mWebView.setDayOrNight(isDayMode);
                    mWebView.reload();
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWebView != null && mWebView.canGoBack())
                    mWebView.goBack();
            }
        });

        mForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWebView != null && mWebView.canGoForward())
                    mWebView.goForward();
            }
        });

        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = mUrl.getText().toString();
                mWebView.loadUrl(url);
                mWebView.requestFocus();
            }
        });

        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUrl == null) {
                    currentUrl = mWebView.getUrl();
                    mUrl.setText("浏览器设置");
                    mWebView.loadUrl("http://debugx5.qq.com/");
                    mMore.setBackgroundColor(pressColor);
                } else {
                    mWebView.loadUrl(currentUrl);
                    currentUrl = null;
                    mMore.setBackgroundColor(normalColor);

                }
            }
        });


        mUrl.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return false;
            }
        });

        mUrl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mGo.setVisibility(View.VISIBLE);
                    if (null == mWebView.getUrl())
                        return;
                    if (mWebView.getUrl().equalsIgnoreCase(mHomeUrl)) {
                        mUrl.setText("");
                        mGo.setText("首页");
                        mGo.setTextColor(colorPrimary);
                        mGo.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, null));

                    } else {
                        mUrl.setText(mWebView.getUrl());
                        mUrl.setSelectAllOnFocus(true);
                        mUrl.selectAll();
                        mGo.setText("进入");
                        mGo.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
                        mGo.setBackgroundColor(colorPrimary);
                    }
                } else {
                    mGo.setVisibility(View.GONE);
                    String title = mWebView.getTitle();
                    if (title != null && title.length() > MAX_LENGTH)
                        mUrl.setText(title.subSequence(0, MAX_LENGTH) + "...");
                    else
                        mUrl.setText(title);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        mUrl.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {


                String url = null;
                if (mUrl.getText() != null) {
                    url = mUrl.getText().toString();
                }

                if (url == null
                        || mUrl.getText().toString().equalsIgnoreCase("")) {
                    //mGo.setText("请输入网址");
                    mGo.setTextColor(colorPrimary);
                    mGo.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
                } else {
                    mGo.setText("进入");
                    mGo.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
                    mGo.setBackgroundColor(colorPrimary);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {


            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {


            }
        });

        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWebView != null)
                    mWebView.loadUrl(mHomeUrl);
            }
        });

        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Process.killProcess(Process.myPid());
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("X5浏览器");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mTestHandler != null)
                            mTestHandler.removeCallbacksAndMessages(null);
                        if (mWebView != null)
                            mWebView.destroy();
                        finish();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.setMessage("退出网页浏览？");
                dialog.create().show();
            }
        });

        mBack.setOnTouchListener(touchListener);
        mForward.setOnTouchListener(touchListener);
        mExit.setOnTouchListener(touchListener);
        mHome.setOnTouchListener(touchListener);
        mMore.setOnTouchListener(touchListener);

    }

    boolean[] m_selected = new boolean[]{true, true, true, true, false,
            false, true};

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView != null && mWebView.canGoBack()) {
                mWebView.goBack();
                if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 16)
                    changGoForwardButton(mWebView);
                return true;
            } else
                return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TbsLog.d(TAG, "onActivityResult, requestCode:" + requestCode
                + ",resultCode:" + resultCode);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    if (null != uploadFile) {
                        Uri result = data == null || resultCode != RESULT_OK ? null
                                : data.getData();
                        uploadFile.onReceiveValue(result);
                        uploadFile = null;
                    }
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (null != uploadFile) {
                uploadFile.onReceiveValue(null);
                uploadFile = null;
            }

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent == null || mWebView == null || intent.getData() == null)
            return;
        mWebView.loadUrl(intent.getData().toString());
    }

    @Override
    protected void onDestroy() {
        if (mTestHandler != null)
            mTestHandler.removeCallbacksAndMessages(null);
        if (mWebView != null)
            mWebView.destroy();
        super.onDestroy();
    }

    public static final int MSG_OPEN_TEST_URL = 0;
    public static final int MSG_INIT_UI = 1;
    private final int mUrlStartNum = 0;
    private int mCurrentUrl = mUrlStartNum;
    private Handler mTestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OPEN_TEST_URL:
                    if (!mNeedTestPage) {
                        return;
                    }

                    String testUrl = "file:///sdcard/outputHtml/html/"
                            + Integer.toString(mCurrentUrl) + ".html";
                    if (mWebView != null) {
                        mWebView.loadUrl(testUrl);
                    }

                    mCurrentUrl++;
                    break;
                case MSG_INIT_UI:
                    init();
                    break;
            }
            super.handleMessage(msg);
        }
    };


    private View.OnTouchListener touchListener = (new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.setBackgroundColor(pressColor);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                view.setBackgroundColor(normalColor);
            }

            return false;
        }
    });

}
