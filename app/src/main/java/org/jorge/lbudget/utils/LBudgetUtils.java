/*
 * This file is part of LBudget.
 * LBudget is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU General Public License as published by
 * the Free Software Foundation
 * LBudget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with LBudget. If not, see <http://www.gnu.org/licenses/>.
 */

package org.jorge.lbudget.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import org.jorge.lbudget.R;
import org.jorge.lbudget.devutils.DevUtils;
import org.jorge.lbudget.ui.activities.InitialActivity;

import java.lang.reflect.Field;

public abstract class LBudgetUtils {

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

    public static String[] getStringArray(Context context, String variableName) {
        String[] ret = null;

        try {
            Field resourceField = R.array.class.getDeclaredField(variableName);
            int resourceId = resourceField.getInt(resourceField);
            ret = context.getResources().getStringArray(resourceId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            DevUtils.showTrace("debug", e);
        }

        return ret;
    }

    public static String getString(Context context, String variableName) {
        String ret = null;

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

    public static int getDrawableAsId(String variableName) {
        int ret = -1;

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

//    public static int getInt(Context context, String variableName) {
//        int ret = -1;
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

    public static String printifyAmount(long amount) {
        return String.valueOf(Math.abs(amount / 100));
    }
}
