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

package org.jorge.lbudget.logic;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupManager;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;

import org.jorge.lbudget.utils.LBudgetUtils;

public abstract class LBackupAgent extends BackupAgentHelper {

    private static final String PREFERENCES_BACKUP_KEY = "PREFERENCES_BACKUP_KEY";

    @Override
    public void onCreate() {
        Context appContext = getApplicationContext();

        String[] allPreferences = LBudgetUtils.getStringArray(appContext, "all_preferences");
        SharedPreferencesBackupHelper sharedPreferencesBackupHelper = new SharedPreferencesBackupHelper(appContext, allPreferences);
        addHelper(PREFERENCES_BACKUP_KEY, sharedPreferencesBackupHelper);

        //TODO Add the database and the pictures
    }

    public static void requestBackup(Context appContext) {
        BackupManager bm = new BackupManager(appContext);
        bm.dataChanged();
    }
}
