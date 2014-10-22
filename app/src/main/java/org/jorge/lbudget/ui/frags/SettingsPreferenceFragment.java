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

package org.jorge.lbudget.ui.frags;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.jorge.lbudget.R;
import org.jorge.lbudget.io.net.LBackupAgent;
import org.jorge.lbudget.logic.controllers.MovementManager;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.concurrent.Executors;

public class SettingsPreferenceFragment extends PreferenceFragment {

    private Context mContext;
    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mContext = mActivity.getApplicationContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        final Preference currencyPreference = findPreference(LBudgetUtils.getString(mContext, "pref_key_currency_code")),
                incomeColorPreference = findPreference(LBudgetUtils.getString(mContext, "pref_key_movement_income_color")),
                expenseColorPreference = findPreference(LBudgetUtils.getString(mContext, "pref_key_movement_expense_color")),
                exportPreference = findPreference(LBudgetUtils.getString(mContext, "pref_key_export_csv"));

        currencyPreference.setSummary(preferences.getString(LBudgetUtils.getString(mContext, "pref_key_currency_code"), LBudgetUtils.getString(mContext, "currency_172")));
        currencyPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                currencyPreference.setSummary((CharSequence) o);
                LBackupAgent.requestBackup(mContext);
                return Boolean.TRUE;
            }
        });

        incomeColorPreference.setSummary(LBudgetUtils.capitalizeFirst(preferences.getString(LBudgetUtils.getString(mContext, "pref_key_movement_income_color"), LBudgetUtils.getString(mContext, "movement_color_green_identifier"))));
        incomeColorPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                incomeColorPreference.setSummary(LBudgetUtils.capitalizeFirst((CharSequence) o));
                LBackupAgent.requestBackup(mContext);
                return Boolean.TRUE;
            }
        });

        expenseColorPreference.setSummary(LBudgetUtils.capitalizeFirst(preferences.getString(LBudgetUtils.getString(mContext, "pref_key_movement_expense_color"), LBudgetUtils.getString(mContext, "movement_color_red_identifier"))));
        expenseColorPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                expenseColorPreference.setSummary(LBudgetUtils.capitalizeFirst((CharSequence) o));
                LBackupAgent.requestBackup(mContext);
                return Boolean.TRUE;
            }
        });

        exportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AsyncTask<Activity, Void, Void>() {
                    @Override
                    protected Void doInBackground(Activity... activities) {
                        activities[0].runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, LBudgetUtils.getString(mContext, MovementManager.getInstance().exportMovementsAsCSV(mContext) ? "csv_export_successful" : "csv_export_unsuccessful"), Toast.LENGTH_LONG).show();
                            }
                        });
                        return null;
                    }
                }.executeOnExecutor(Executors.newSingleThreadExecutor(), mActivity);
                return Boolean.TRUE;
            }
        });
    }
}
