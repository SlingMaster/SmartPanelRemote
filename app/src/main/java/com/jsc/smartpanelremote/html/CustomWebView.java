/*
 * Copyright (c) 2020
 * Jeneral Samopal Company
 * Programming by Alex Uchitel
 */
package com.jsc.smartpanelremote.html;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.http.SslError;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import androidx.annotation.Nullable;

public class CustomWebView extends WebView {
    public interface WebEvents {
        void webViewEvents(int request, final String jsonString);
    }

    @Nullable
    private WebEvents webListener = null;

    protected JSOut jsOut;

    public CustomWebView(Context context) {
        super(context);
        init();
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

//    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//      init();
//    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void init() {
        // set WebView Style background and scrollbar ----
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        //webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        //webView.setScrollbarFadingEnabled(false);
        // set default black color ------------
        setBackgroundColor(Color.TRANSPARENT);

        // web settings --------------------------------------
        WebSettings webSettings = getSettings();

        // ??? ---------------------------------------
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        // ??? ---------------------------------------

        // включаем поддержку JavaScript
        getSettings().setJavaScriptEnabled(true);
        // определим экземпляр MyWebViewClient.
        // Он может находиться в любом месте после инициализации объекта WebView
        setWebViewClient(new MyWebViewClient());

        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setAllowFileAccess(true);


        // set cash app ---------------------------------------
        webSettings.setAppCacheEnabled(true);
        // set wiewport scale ---------------
        // webSettings.setUseWideViewPort(isWideViewPortRequired());
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        JSIn jsIn = new JSIn();
        addJavascriptInterface(jsIn, JSConstants.INTERFACE_NAME);

        // js interface --------------------------------------
        jsOut = new JSOut(this);

    }


    // ----------------------------------------
    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // System.out.println("[ trace  ] onPage Finished : " + url);
            final WebEvents listener = webListener;
            if(listener!=null) {
                listener.webViewEvents(JSConstants.EVT_PAGE_FINISHED, "{}");
            }
        }
//
//        @Override
//        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError er) {
//            handler.proceed();
//        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            System.out.println("onReceived SslError");
            handler.proceed();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            System.out.println("onReceived Error : " + error);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    public void setWebEventsListener(@Nullable WebEvents listener) {
        webListener = listener;
    }

    // command only ---------------------------
    public void callbackToUI(int target) {
        if (jsOut != null) {
            jsOut.callJavaScript(target, new JSONObject());
        } else {
            System.out.println("trace | Error Missing JSInterface");
        }
    }

    // ----------------------------------------
    public void callbackToUI(int target, JSONObject json) {
        if (jsOut != null) {
            jsOut.callJavaScript(target, json);
        } else {
            System.out.println("trace | Error Missing JSInterface");
        }
    }

    // ===================================================
    // Create response for HTML UI
    // ===================================================
    public static JSONObject createResponse(JSONObject request, JSONObject response) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(JSConstants.REQUEST, request);
            obj.put(JSConstants.RESPONSE, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }


    // =========================================================
    // Interface HTML > Application
    // =========================================================
    private class JSIn {
        @JavascriptInterface
        public final void callNative(int request, final String jsonString) {
            // HTML function send data to activity -----------------------------
            final WebEvents listener = webListener;
            if(listener!=null) {
                listener.webViewEvents(request, jsonString);
            }
        }
    }
}
