package com.example.webviewvideo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import wendu.dsbridge.CompletionHandler;
import wendu.dsbridge.DWebView;

public class MainActivity extends Activity {

    private static final String TAG = "Activity" ;
    private  DWebView dWebView;
    private String url = "file:///android_asset/index.html";
//    private  String url = "http://www.le.com/ptv/vplay/67195939.html";
    // 用来显示视频的布局

    private FrameLayout frameLayout;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout)findViewById(R.id.video_frame);
        dWebView = (DWebView) findViewById(R.id.video_dwebview);

//        Bundle data = new Bundle();
//        data.putBoolean("standardFullScreen", false);
//        //true表示标准全屏，false表示X5全屏；不设置默认false，
//        data.putBoolean("supportLiteWnd", false);
//        //false：关闭小窗；true：开启小窗；不设置默认true，
//        data.putInt("DefaultVideoScreen", 2);
//        //1：以页面内开始播放，2：以全屏开始播放；不设置默认：1
//        dWebView.getX5WebViewExtension().invokeMiscMethod("setVideoParams", data);

        initDWebView();

        dWebView.loadUrl(url);

    }


    /**
     * 横竖屏切换监听
     */
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        switch (config.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dWebView.destroy();
        dWebView = null;
    }


    private void initDWebView(){

        dWebView.setWebContentsDebuggingEnabled(true);
        dWebView.setVerticalScrollBarEnabled(false); // 取消Vertical ScrollBar显示
        dWebView.setHorizontalScrollBarEnabled(false); // 取消Horizontal ScrollBar显示
        dWebView.setFocusable(false); // 去掉超链接的外边框
        dWebView.setWebViewClient(new MyWebViewClient());
        dWebView.setWebChromeClient(new MyWebChromeClient());
        dWebView.addJavascriptObject(new JsCallNative(), null);

        WebSettings webSetting = dWebView.getSettings();
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);//设置webview缓存模式
        //设置自适应屏幕，两者合用
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAllowFileAccess(true);// 允许访问文件
        webSetting.setAllowFileAccessFromFileURLs(true);
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSetting.setDefaultTextEncodingName("utf-8");//设置文本编码（根据页面要求设置： utf-8）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSetting.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    private class JsCallNative{

        @SuppressLint("SourceLockedOrientationActivity")
        @JavascriptInterface
        public void setFullScreen(Object msg, CompletionHandler<String> handler) {
            handler.complete(msg+"回调给js");
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                frameLayout.setVisibility(View.GONE);
            }
            else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                frameLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        /**
         * 防止加载网页时调起系统浏览器
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };


    private class MyWebChromeClient extends WebChromeClient {

        private IX5WebChromeClient.CustomViewCallback mCustomViewCallback;
        //  横屏时，显示视频的view
        private View mCustomView;

        // 全屏的时候调用
        @SuppressLint("SourceLockedOrientationActivity")
        @Override
        public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {

            //如果view 已经存在，则隐藏
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mCustomView = view;
            mCustomView.setVisibility(View.VISIBLE);
            mCustomViewCallback = callback;
            frameLayout.addView(mCustomView);
            frameLayout.setVisibility(View.VISIBLE);
            frameLayout.bringToFront();
            dWebView.setVisibility(View.GONE);

            //设置横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            super.onShowCustomView(view, callback);

        }

        // 切换为竖屏的时候调用
        @SuppressLint("SourceLockedOrientationActivity")
        @Override
        public void onHideCustomView() {

            super.onHideCustomView();
            if (mCustomView == null) {
                return;
            }
            mCustomView.setVisibility(View.GONE);
            frameLayout.removeView(mCustomView);
            mCustomView = null;
            frameLayout.setVisibility(View.GONE);
            dWebView.setVisibility(View.VISIBLE);
            try {
                mCustomViewCallback.onCustomViewHidden();
            } catch (Exception e) {
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏

        }

//        @Override
//        public void onProgressChanged(WebView view, int newProgress) {
//            super.onProgressChanged(view, newProgress);
//            if (newProgress == 100){
//                progress.setVisibility(View.GONE);
//            }else {
//                progress.setVisibility(View.VISIBLE);
//                progress.setProgress(newProgress);
//            }
//        }

    }
}
