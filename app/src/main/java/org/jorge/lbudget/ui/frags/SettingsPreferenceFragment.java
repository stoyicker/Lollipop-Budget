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

package org.jorge.lbudget.ui.frags;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import org.jorge.lbudget.R;
import org.jorge.lbudget.utils.LBudgetUtils;

public class SettingsPreferenceFragment extends PreferenceFragment {

    private Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        final Preference incomeColorPref = findPreference(LBudgetUtils.getString(mContext, "pref_key_movement_income_color")), expenseColorPref = findPreference(LBudgetUtils.getString(mContext, "pref_key_movement_expense_color"));
        incomeColorPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Toast.makeText(mContext, R.string.setting_updated_upon_restart, Toast.LENGTH_LONG).show();
                return Boolean.TRUE;
            }
        });
        expenseColorPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Toast.makeText(mContext, R.string.setting_updated_upon_restart, Toast.LENGTH_LONG).show();
                return Boolean.TRUE;
            }
        });
    }
}
