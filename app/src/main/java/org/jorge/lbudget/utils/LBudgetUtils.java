/*
 * This file is part of LBudget.
 * LBudget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * LBudget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with LBudget. If not, see <http://www.gnu.org/licenses/>.
 */

package org.jorge.lbudget.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import org.jorge.lbudget.R;
import org.jorge.lbudget.devutils.DevUtils;
import org.jorge.lbudget.ui.activities.InitialActivity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.jorge.lbudget.devutils.DevUtils.logString;

public abstract class LBudgetUtils {

    private static final Map<String, Charset> charsetMap = new HashMap<>();

    public static String getCurrentForegroundActivityClass(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        logString("debug", "Class in top: " + taskInfo.get(0).topActivity.getClassName());
        return taskInfo.get(0).topActivity.getClassName();
    }

    /**
     * This is bad, very bad, but the problems only show on some devices and my time window is gone.
     */
    public static void configureStrictMode() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static Charset getLocaleCharset(String locale) {
        return charsetMap.containsKey(locale) ? charsetMap.get(locale) :
                Charset.forName(locale);
    }

    public static int dpToPx(Resources res, int dp) {
        return (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    public static void restartApp(Activity activity) {
        activity.startActivity(new Intent(activity, InitialActivity.class));
        Intent i = activity.getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(i);
    }

    public static String getRealm(Context context) {

        return context != null ? PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString(
                "pref_title_server",
                "euw").toLowerCase(Locale.ENGLISH) : "";
    }

    public static String getLocale(Context context) {
        String ret;
        try {
            ret = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                    .getString("pref_title_locale", "en_US");
        } catch (NullPointerException ex) {
            ret = null;
        }

        return ret;
    }

//    public static String[] getStringArray(Context context, String variableName,
//                                          String[] defaultRet) {
//        String[] ret = defaultRet;
//
//        try {
//            Field resourceField = R.array.class.getDeclaredField(variableName);
//            int resourceId = resourceField.getInt(resourceField);
//            ret = context.getResources().getStringArray(resourceId);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            DevUtils.showTrace("debug", e);
//        }
//
//        return ret;
//    }

    public static String getString(Context context, String variableName, String defaultRet) {
        String ret = defaultRet;

        try {
            Field resourceField = R.string.class.getDeclaredField(variableName);
            int resourceId = resourceField.getInt(resourceField);
            if (context != null)
                ret = context.getString(resourceId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            DevUtils.showTrace("debug", e);
        }

        return ret;
    }

    public static int getDrawableAsId(String variableName, int defaultRet) {
        int ret = defaultRet;

        try {
            Field resourceField = R.drawable.class.getDeclaredField(variableName);
            ret = resourceField.getInt(resourceField);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            DevUtils.showTrace("debug", e);
        }

        return ret;
    }

    public static Boolean isInternetReachable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean ret;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI),
                dataNetworkInfo =
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        Boolean isWifiConnected =
                (wifiNetworkInfo == null) ? Boolean.FALSE : wifiNetworkInfo.isConnected(),
                isDataConnected =
                        (dataNetworkInfo == null) ? Boolean.FALSE :
                                dataNetworkInfo.isConnected();
        ret = isWifiConnected || (preferences
                .getBoolean("pref_title_data",
                        Boolean.FALSE) && isDataConnected);

        return ret;
    }

//    public static int getInt(Context context, String variableName, int defaultRet) {
//        int ret = defaultRet;
//
//        try {
//            Field resourceField = R.integer.class.getDeclaredField(variableName);
//            int resourceId = resourceField.getInt(resourceField);
//            ret = context.getResources().getInteger(resourceId);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            DevUtils.showTrace("debug", e);
//        }
//
//        return ret;
//    }

    public static int pixelsAsDp(Context context, int sizeInPx) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (sizeInPx * scale + 0.5f);
    }

    public static String inputStreamAsString(InputStream is, String locale) throws IOException {
        java.util.Scanner s =
                new java.util.Scanner(is, LBudgetUtils.getLocaleCharset(locale).name());
        String ret;
        ret = s.useDelimiter("\\A").hasNext() ? s.next() : "";
        return ret;
    }
}
