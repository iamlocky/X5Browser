package com.lockyluo.x5corelibrary;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.lockyluo.x5corelibrary.utils.DensityUtils;
import com.lockyluo.x5corelibrary.utils.ItemLongClickedPopWindow;
import com.lockyluo.x5corelibrary.utils.SaveImage;
import com.lockyluo.x5corelibrary.utils.StatusBarCompat;
import com.lockyluo.x5corelibrary.utils.StringUtils;
import com.lockyluo.x5corelibrary.utils.ToastUtil;
import com.lockyluo.x5corelibrary.utils.X5WebView;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient.CustomViewCallback;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.utils.TbsLog;

import java.net.MalformedURLException;
import java.net.URL;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


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
    private InputMethodManager imm;
    private boolean mNeedTestPage = false;
    private int downX = 100;
    private int downY = 100;
    private final int disable = 120;
    private final int enable = 255;
    private ProgressBar mPageLoadingProgressBar = null;

    private ValueCallback<Uri> uploadFile;

    private URL mIntentUrl;
    private String imgurl;
    private static final String TAG = "X5Sdk";
    private static final int MAX_LENGTH = 18;
    private static final int pressColor = 0x4C000000;
    private static int normalColor = 0xECF0F2;
    private static boolean isDayMode = true;
    public static int colorPrimary = 0;
    private static String mHomeUrl = "http://debugx5.qq.com/";

    public static void init(@NonNull Activity context, @NonNull boolean isDayMode, int defaultColorPrimary) {//不带url启动


        if (isHexNumber(defaultColorPrimary + "")) {
            setColorPrimary(defaultColorPrimary);
            Log.d("init", "-----------defaultColorPrimary-----------");
            Log.d("init", "----------" + defaultColorPrimary + "-----------");
        }
        setDayMode(isDayMode);
        context.startActivity(new Intent(context, BrowserActivity.class));
    }

    public static void init(@NonNull Activity context, @NonNull String url, @NonNull boolean isDayMode, int defaultColorPrimary) {//带url启动
        mHomeUrl = checkUrl(url);
        init(context, isDayMode, defaultColorPrimary);
    }

    private static void setDayMode(boolean dayMode) {
        isDayMode = dayMode;
    }

    public void restart() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            manager.restartPackage(getPackageName());
        }
    }

    private static String checkUrl(@NonNull String url) {//检查url是否正确
        if (!TextUtils.isEmpty(url)) {
            try {
                if (!url.startsWith("http")) {
                    url = "http://" + url.trim();
                }
                {
                    url.replaceAll(" ", "")
                            .replaceAll("\n", "");//去除中文和空格换行
                }
                {
                    url = StringUtils.fullToHalf(url);
                }
                url = (new URL(url).toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                url = "http://m.baidu.com";
            }
        }
        return url;
    }

    private static void setColorPrimary(int defaultColorPrimary) {

        colorPrimary = defaultColorPrimary;
    }


    //判断十六进制
    private static boolean isHexNumber(@NonNull String str) {
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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        }


        try {//检查颜色是否为0
            if (colorPrimary == 0) {
                setColorPrimary(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()));
                normalColor = ResourcesCompat.getColor(getResources(), R.color.day, getTheme());

            } else {
                Log.d("color", colorPrimary + "");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    Window window=getWindow();
//                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                    window.setStatusBarColor(colorPrimary);
                StatusBarCompat.compat(this, colorPrimary);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Intent intent = getIntent();
        if (intent != null) {//intent方式获取url
            final Uri uri = intent.getData();

            try {
                if (uri != null) {
                    mIntentUrl = new URL(uri.toString());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //
        try {
            if (Integer.parseInt(Build.VERSION.SDK) >= 11) {
                getWindow()
                        .setFlags(
                                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_browser);
        mViewParent = (ViewGroup) findViewById(R.id.webView1);

        //-------------------------------------------------initview
        initBtnListenser();
        initColor();


        mTestHandler.sendEmptyMessageDelayed(MSG_INIT_UI, 10);

    }

    private void initColor() {
        LinearLayout navi = findViewById(R.id.navigation1);
        navi.setBackgroundColor(colorPrimary);

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
        mPageLoadingProgressBar.setBackgroundColor(colorPrimary);

    }

    private void init() {

        mWebView = new X5WebView(this, null) {
            @Override
            public void loadUrl(String s) {//----------重写，添加检查url合法性
                s = checkUrl(s);
                mWebView.requestFocus();
                mGo.setVisibility(GONE);
                super.loadUrl(s);
            }
        };

        mViewParent.addView(mWebView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        initProgressBar();

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mUrl.setText(url);
                Log.d(TAG, "shouldOverrideUrlLoading: " + url);
                return false;
            }

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
                Log.d(TAG, "onPageStarted: " + s);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Log.d(TAG, "onPageFinished: " + url);
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
                if (i >= 99) {
                    mPageLoadingProgressBar.setVisibility(View.GONE);
                } else {
                    if (mPageLoadingProgressBar.getVisibility() != View.VISIBLE) {
                        mPageLoadingProgressBar.setVisibility(View.VISIBLE);
                    }
                }
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
                    imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    mWebView.requestFocus();
                }
                return false;


            }
        });

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                downX = (int) motionEvent.getX();
                downY = (int) motionEvent.getY();
                return false;
            }
        });
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final ItemLongClickedPopWindow itemLongClickedPopWindow;
                final WebView.HitTestResult result = mWebView.getHitTestResult();
                if (null == result)
                    return false;
                int type = result.getType();
                if (type == WebView.HitTestResult.UNKNOWN_TYPE)
                    return false;
                if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
                    //let TextViewhandles context menu return true;
                }
                itemLongClickedPopWindow = new ItemLongClickedPopWindow(BrowserActivity.this, ItemLongClickedPopWindow.IMAGE_VIEW_POPUPWINDOW, DensityUtils.dip2px(getApplicationContext(), 120), DensityUtils.dip2px(getApplicationContext(), 90));
                // Setup custom handlingdepending on the type
                switch (type) {
                    case WebView.HitTestResult.PHONE_TYPE: // 处理拨号
                        break;
                    case WebView.HitTestResult.EMAIL_TYPE: // 处理Email
                        break;
                    case WebView.HitTestResult.GEO_TYPE: // TODO
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE: // 超链接
                        // Log.d(DEG_TAG, "超链接");
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                        break;
                    case WebView.HitTestResult.IMAGE_TYPE: // 处理长按图片的菜单项
                        imgurl = result.getExtra();
                        //通过GestureDetector获取按下的位置，来定位PopWindow显示的位置
                        itemLongClickedPopWindow.showAtLocation(view, Gravity.TOP | Gravity.LEFT, downX, downY + 10);
                        break;
                    default:
                        break;
                }

                itemLongClickedPopWindow.getView(R.id.item_longclicked_viewImage)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                itemLongClickedPopWindow.dismiss();
                                mWebView.loadUrl(result.getExtra());
                            }
                        });

                itemLongClickedPopWindow.getView(R.id.item_longclicked_saveImage)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                itemLongClickedPopWindow.dismiss();
                                new SaveImage().execute(result.getExtra());
                            }
                        });

                return false;
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String arg0, String s1, String s2, String s3, long l) {
                {//部分x5内核无效
                    TbsLog.d(TAG, "url: " + arg0);
                    final String url = arg0;
                    new AlertDialog.Builder(BrowserActivity.this)
                            .setTitle("是否下载？")
                            .setMessage(arg0)
                            .setPositiveButton("立即下载", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Uri uri = Uri.parse(url);
                                    Intent downloadIntent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(downloadIntent);

                                    ToastUtil.show(
                                            context,
                                            "开始下载");
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ToastUtil.show(
                                            context,
                                            "取消下载");
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {

                                }
                            }).show();
                }
            }
        });

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

        if (mWebView.getX5WebViewExtension() == null) {
            ToastUtil.show(context, "x5内核启动失败");
        }

        setMode();
    }

    private void setMode() {//设置夜间模式开关
        {
            mWebView.setDayOrNight(isDayMode);
            if (isDayMode) {
                mMode.setBackgroundColor(colorPrimary);
                mMode.setText("夜");

                mBack.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                mForward.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                mExit.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                mHome.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                mMore.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                mToolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.day, null));
                normalColor = ResourcesCompat.getColor(getResources(), R.color.day, null);

            } else {
                mMode.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                //mMode.setText("日");

                mBack.setBackgroundColor(colorPrimary);
                mForward.setBackgroundColor(colorPrimary);
                mExit.setBackgroundColor(colorPrimary);
                mHome.setBackgroundColor(colorPrimary);
                mMore.setBackgroundColor(colorPrimary);
                mToolbar.setBackgroundColor(colorPrimary);

                normalColor = colorPrimary;
            }
            mWebView.reload();
        }
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
        mGo.setBackgroundColor(colorPrimary);
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
                        isDayMode = false;//--------------is夜间模式
                        setMode();
                    } else {
                        isDayMode = true;//--------------is日间模式
                        setMode();
                    }
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
                    imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

        mUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.ACTION_DOWN || i == EditorInfo.IME_ACTION_GO) {
                    mWebView.loadUrl(mUrl.getText().toString());

                    //mWebView.requestFocus();
                }
                return true;
            }
        });

        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWebView != null)
                    mWebView.loadUrl(mHomeUrl);
            }
        });

        mHome.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                restart();
                return true;
            }
        });

        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                        //Process.killProcess(Process.myPid());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ToastUtil.show(getApplicationContext(), "获取读写内部存储权限失败");
        }
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
    protected void onDestroy() {//销毁时重置
        if (mTestHandler != null)
            mTestHandler.removeCallbacksAndMessages(null);
        if (mWebView != null)
            mWebView.destroy();
        Log.d("locky ", "销毁browser");
        setContentView(new View(BrowserActivity.this));
        colorPrimary = 0;
        isDayMode = true;
        mHomeUrl = "http://m.baidu.com";
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
