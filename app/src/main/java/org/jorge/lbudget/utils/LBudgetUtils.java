/*
 * This file is part of Lollipop Budget.
 * Lollipop Budget is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU General Public License as published by
 * the Free Software Foundation
 * Lollipop Budget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Lollipop Budget. If not, see <http://www.gnu.org/licenses/>.
 */

package org.jorge.lbudget.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import com.crashlytics.android.Crashlytics;

import org.jorge.lbudget.R;
import org.jorge.lbudget.io.db.SQLiteDAO;
import org.jorge.lbudget.logic.adapters.AccountListRecyclerAdapter;
import org.jorge.lbudget.logic.adapters.MovementListRecyclerAdapter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public abstract class LBudgetUtils {
//
//    public static int dpAsPixels(Resources res, int dp) {
//        return (int) TypedValue
//                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
//    }
//
//    public static void restartApp(Activity activity) {
//        activity.startActivity(new Intent(activity, InitialActivity.class));
//        Intent i = activity.getBaseContext().getPackageManager()
//                .getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        activity.startActivity(i);
//    }

    public static String[] getStringArray(Context context, String variableName) {
        String[] ret = null;

        try {
            Field resourceField = R.array.class.getDeclaredField(variableName);
            int resourceId = resourceField.getInt(resourceField);
            ret = context.getResources().getStringArray(resourceId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Crashlytics.logException(e);
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
            Crashlytics.logException(e);
        }

        return ret;
    }

    public static int getDrawableAsId(String variableName) {
        int ret = -1;

        try {
            Field resourceField = R.drawable.class.getDeclaredField(variableName);
            ret = resourceField.getInt(resourceField);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Crashlytics.logException(e);
        }

        return ret;
    }

//    public static Boolean isInternetReachable(Context context) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        Boolean ret;
//
//        ConnectivityManager connectivityManager =
//                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo wifiNetworkInfo =
//                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI),
//                dataNetworkInfo =
//                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//        Boolean isWifiConnected =
//                (wifiNetworkInfo == null) ? Boolean.FALSE : wifiNetworkInfo.isConnected(),
//                isDataConnected =
//                        (dataNetworkInfo == null) ? Boolean.FALSE :
//                                dataNetworkInfo.isConnected();
//        ret = isWifiConnected || (preferences
//                .getBoolean("pref_title_data",
//                        Boolean.FALSE) && isDataConnected);
//
//        return ret;
//    }

    public static int getInt(Context context, String variableName) {
        int ret = -1;

        try {
            Field resourceField = R.integer.class.getDeclaredField(variableName);
            int resourceId = resourceField.getInt(resourceField);
            ret = context.getResources().getInteger(resourceId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Crashlytics.logException(e);
        }

        return ret;
    }

    public static int getColor(Context context, String variableName) {
        int ret = -1;

        try {
            Field resourceField = R.color.class.getDeclaredField(variableName);
            int resourceId = resourceField.getInt(resourceField);
            ret = context.getResources().getColor(resourceId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            //Used for field discovery, so no crash to report
        }

        return ret;
    }

//    public static int pixelsAsDp(Context context, int sizeInPx) {
//        float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (sizeInPx * scale + 0.5f);
//    }
//
//    public static ValueAnimator createStandardCircularReveal(View v) {
//        return ViewAnimationUtils.createCircularReveal(v, (v.getLeft() + v.getRight()) / 2, (v.getTop() + v.getBottom()) / 2, 0, v.getWidth());
//    }
//
//    public static ValueAnimator createStandardCircularHide(View v) {
//        return ViewAnimationUtils.createCircularReveal(v, (v.getLeft() + v.getRight()) / 2, (v.getTop() + v.getBottom()) / 2, v.getWidth(), 0);
//    }

    public static int calculateAvailableAccountId() {
        List<AccountListRecyclerAdapter.AccountDataModel> allAcc = SQLiteDAO.getInstance().getAccounts();
        Collections.sort(allAcc, new Comparator<AccountListRecyclerAdapter.AccountDataModel>() {
            @Override
            public int compare(AccountListRecyclerAdapter.AccountDataModel acc1, AccountListRecyclerAdapter.AccountDataModel acc2) {
                return acc1.getAccountId() - acc2.getAccountId();
            }
        });
        if (allAcc.size() == 1)
            return allAcc.get(0).getAccountId() == 1 ? 2 : 1;
        for (int i = 0; i < allAcc.size() - 1; i++) {
            int candidate;
            if ((candidate = allAcc.get(i).getAccountId() + 1) != allAcc.get(i + 1).getAccountId()) {
                return candidate;
            }
        }
        return allAcc.size();
    }

    public static int calculateAvailableMovementId() {
        List<MovementListRecyclerAdapter.MovementDataModel> allMovementsOnSelectedAcc = SQLiteDAO.getInstance().getSelectedAccountMovements();
        Collections.sort(allMovementsOnSelectedAcc, new Comparator<MovementListRecyclerAdapter.MovementDataModel>() {
            @Override
            public int compare(MovementListRecyclerAdapter.MovementDataModel movementDataModel1, MovementListRecyclerAdapter.MovementDataModel movementDataModel2) {
                return movementDataModel1.getMovementId() - movementDataModel2.getMovementId();
            }
        });
        if (allMovementsOnSelectedAcc.size() == 1)
            return allMovementsOnSelectedAcc.get(0).getMovementId() == 1 ? 2 : 1;
        for (int i = 0; i < allMovementsOnSelectedAcc.size() - 1; i++) {
            int candidate;
            if ((candidate = allMovementsOnSelectedAcc.get(i).getMovementId() + 1) != allMovementsOnSelectedAcc.get(i + 1).getMovementId()) {
                return candidate;
            }
        }
        return allMovementsOnSelectedAcc.size();
    }

    public static CharSequence capitalizeFirst(CharSequence movementTitle) {
        if (TextUtils.isEmpty(movementTitle)) return movementTitle;
        return String.valueOf(movementTitle.charAt(0)).toUpperCase(Locale.ENGLISH) + movementTitle.toString().substring(1).toLowerCase(Locale.ENGLISH);
    }

    public static boolean adapterContains(ArrayAdapter<String> adapter, String month) {
        int count = adapter.getCount();
        for (int i = 0; i < count; i++)
            if (month.contentEquals(adapter.getItem(i))) return true;
        return false;
    }
}
