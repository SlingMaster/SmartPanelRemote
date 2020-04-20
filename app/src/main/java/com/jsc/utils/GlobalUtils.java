/*
 * Copyright (c) 2020.
 * RF Controls
 * Design and Programming by Alex Dovby
 */

package com.jsc.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class GlobalUtils {
    private static Boolean isTabletModeDetermined = false;
    private static Boolean isTabletMode = false;

    // ========================================
    public static boolean isTablet(Context paramContext) {
        if (!isTabletModeDetermined) {
            if (paramContext.getResources().getConfiguration().smallestScreenWidthDp >= 600)
                isTabletMode = true;
            isTabletModeDetermined = true;
        }
        return isTabletMode;
    }

    // ===================================================
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                return false;
            }
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } else {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo == null) {
                return false;
            }
            return networkInfo.isConnected();
        }
    }

    // ===================================================
    public static String getString(@NonNull Context context, @StringRes int resource) {
        return context.getResources().getString(resource);
    }
}
