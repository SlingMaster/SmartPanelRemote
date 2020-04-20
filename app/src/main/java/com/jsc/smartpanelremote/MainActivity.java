/*
 * Copyright (c) 2020.
 * Jeneral Samopal Company
 * Design and Programming by Alex Dovby
 */

package com.jsc.smartpanelremote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.TextView;
import android.widget.Toast;

import com.jsc.smartpanelremote.html.CustomWebView;
import com.jsc.smartpanelremote.html.JSConstants;
import com.jsc.utils.FileUtils;
import com.jsc.utils.GlobalUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private static final int BACK = 0x03;
    ViewGroup webContainer;
    private boolean mVisible;
    @Nullable
    CustomWebView webView;
    public static SharedPreferences preference;
    private String sendData;
    private TCPClient mTcpClient;

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
        preference = PreferenceManager.getDefaultSharedPreferences(this);

        mVisible = true;
        webContainer = findViewById(R.id.web_container);
        webContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(view -> {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("cmd", String.valueOf(BACK));
                        runCommand(json);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
        );

        // load screen -----------------------------
        webView = createWebView();
        webContainer.addView(webView);

        // set max font size NORMAL ----------------
        WebSettings webSettings = webView.getSettings();
        System.out.println("trace TextZoom = " + webSettings.getTextZoom());
        if (webSettings.getTextZoom() > 125) {
            webSettings.setTextZoom(125);
        }

        // -----------------------------------------
        webView.clearCache(preference.getBoolean("sw_clear_cache", false));
        String url = preference.getBoolean("sw_debug", false)
                ? getResources().getString(R.string.url_ui_debug)
                : getResources().getString(R.string.url_ui);
        // -----------------------------------------
        webView.loadUrl(url);
    }

    // ===================================
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    // ===================================
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//    }

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

    // ===================================
    @Override
    protected void onStop() {
        super.onStop();
        if (mTcpClient != null) {
            mTcpClient = null;
        }
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
        if (mTcpClient != null) {
            mTcpClient = null;
        }
    }

    // ===================================
    @NonNull
    private CustomWebView createWebView() {
        CustomWebView view = new CustomWebView(this);
        view.setWebEventsListener(this::webViewEvents);
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
                // new ParseTask().execute();
                readJSON();
                break;
            case JSConstants.EVT_UI:
//                Toast.makeText(getApplicationContext(), "EVT • " + request + " | " + jsonString, Toast.LENGTH_LONG).show();
                runCommand(requestContent);
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
    private void runCommand(JSONObject json) {
        // connect to the server =========
        // and send message to the server
        sendData = json.toString();
        new connectTask().execute("");

        if (mTcpClient != null) {
            TextView viewCMD = findViewById(R.id.command_id);
            viewCMD.setText(sendData);
            TextView viewStatus = findViewById(R.id.connect_status);
            viewStatus.setText(getResources().getString(R.string.msg_send));
        }
    }


    // ===================================================
    private class connectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            String tempNumStr = preference.getString("port", "8080");
            int port = Integer.parseInt(tempNumStr);
            String ip = preference.getString("panel_ip", "192.168.1.200");
            mTcpClient.run(ip, port, sendData);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            System.out.println("onProgressUpdate • " + values[0]);
            TextView view = findViewById(R.id.connect_status);
            view.setText(values[0]);
        }
    }

    // =========================================================
    // Read json
    // =========================================================
    private void readJSON() {
        String jsonStr = "";
        JSONObject dataJsonObj = null;

        try {
            jsonStr = FileUtils.readAssetFile(getBaseContext(), getResources().getString(R.string.scr_json));
            try {
                dataJsonObj = new JSONObject(jsonStr);
                if (webView != null) {
                    webView.callbackToUI(JSConstants.APP_DIRECTORY, createResponse(dataJsonObj));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            //IOExpeptino
        }
    }

    // =========================================================
    // Create response for HTML UI
    // =========================================================
    protected JSONObject createResponse(JSONObject response) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(JSConstants.REQUEST, null);
            obj.put(JSConstants.RESPONSE, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }
}