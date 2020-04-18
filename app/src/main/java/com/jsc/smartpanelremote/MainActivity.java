/*
 * Copyright (c) 2020.
 * RF Controls
 * Design and Programming by Alex Dovby
 */

package com.jsc.smartpanelremote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jsc.smartpanelremote.html.CustomWebView;
import com.jsc.smartpanelremote.html.JSConstants;

import com.jsc.utils.GlobalUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    ViewGroup webContainer;
    private boolean mVisible;
    @Nullable
    CustomWebView webView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            webContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    // ===================================
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    // ===================================
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    // ===================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVisible = true;
        webContainer = findViewById(R.id.web_container);
         webContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        // load screen -----------------------------
        webView = createWebView();
        webContainer.addView(webView);
        webView.clearCache(true);
        //webView.loadUrl("http://192.168.1.2/Projects/PanelRemoteCtrl");
        webView.loadUrl("http:\\\\192.168.1.2\\/wl\\/src\\/main.html");
    }

    // ===================================
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    // ===================================
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // ===================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        webContainer.removeAllViews();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }

    // ===================================
    @NonNull
    private CustomWebView createWebView() {
        CustomWebView view = new CustomWebView(this);
        view.setWebEventsListener(this::webViewEvents);
        // SIGNAL 11 SIGSEGV crash Android
//        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        return view;
    }

    // ===================================================
    // HTML APP request events
    // ===================================================
    public void webViewEvents(int request, final String jsonString) {
        JSONObject requestContent = new JSONObject();

        JSONObject uiRequest;
        try {
            uiRequest = new JSONObject(jsonString);
            if (uiRequest.has("request")) {
                requestContent = uiRequest.getJSONObject("request");
            } else {
                requestContent = uiRequest;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (request) {
            case JSConstants.EVT_MAIN_TEST:
                assert webView != null;
                webView.callbackToUI(JSConstants.EVT_MAIN_TEST, CustomWebView.createResponse(requestContent, null));
                break;
            case JSConstants.EVT_READY:
                assert webView != null;
                webView.callbackToUI(JSConstants.CMD_INIT, CustomWebView.createResponse(requestContent, initData(this)));
                break;
            case JSConstants.EVT_RUN_CMD:
                break;
            case JSConstants.EVT_BACK:
                break;
            case JSConstants.EVT_PAGE_FINISHED:
                // onPageFinished();
                break;
            default:
                System.out.println("Unsupported command : " + request);
                break;
        }
    }

    // ----------------------------------------
    public static JSONObject initData(@NonNull Context context) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("android_os", android.os.Build.VERSION.SDK_INT);
            obj.put("language", "en");
            obj.put("phone_ui", !GlobalUtils.isTablet(context));
            obj.put("android_app", true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    // ===================================
    // System UI events
    // ===================================
    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    // ===================================
    private void hide() {
        mVisible = false;
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    // ===================================
    @SuppressLint("InlinedApi")
    private void show() {
        mVisible = true;
        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

    // ===================================
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // ===================================
    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(getSupportActionBar()).hide();
        if (!GlobalUtils.isConnectedToInternet(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.msg_not_wifi_connection), Toast.LENGTH_LONG).show();
        }
    }
}
