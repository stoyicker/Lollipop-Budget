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

package org.jorge.lbudget.io.net;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupManager;
import android.app.backup.FileBackupHelper;
import android.app.backup.RestoreObserver;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.ParcelFileDescriptor;

import org.jorge.lbudget.io.db.SQLiteDAO;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.io.File;
import java.io.IOException;

public class LBackupAgent extends BackupAgentHelper {

    private static final String PREFERENCES_BACKUP_KEY = "PREFERENCES_BACKUP_KEY",
            DATABASE_BACKUP_KEY = "DATABASE_BACKUP_KEY";
    private static BackupManager mBackupManager;

    @Override
    public void onCreate() {
        Context appContext = getApplicationContext();
        mBackupManager = new BackupManager(appContext);

        String[] allPreferences = LBudgetUtils.getStringArray(appContext,
                "backupable_preference_keys");
        SharedPreferencesBackupHelper sharedPreferencesBackupHelper = new
                SharedPreferencesBackupHelper(appContext, allPreferences);
        addHelper(PREFERENCES_BACKUP_KEY, sharedPreferencesBackupHelper);

        File databaseFile = appContext.getDatabasePath(LBudgetUtils.getString(appContext,
                "db_name"));
        if (databaseFile != null && databaseFile.exists()) {
            FileBackupHelper database = new FileBackupHelper(appContext,
                    databaseFile.getAbsolutePath());
            addHelper(DATABASE_BACKUP_KEY, database);
        }
        //The pictures are not synchronized because it would go beyond the Backup API 1 MB limit
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
        synchronized (SQLiteDAO.DB_LOCK) {
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                          ParcelFileDescriptor newState) throws IOException {
        synchronized (SQLiteDAO.DB_LOCK) {
            super.onRestore(data, appVersionCode, newState);
        }
    }

    public static void requestBackup(Context appContext) {
        BackupManager bm = new BackupManager(appContext);
        bm.dataChanged();
    }

    public static void restoreBackup() {
        mBackupManager.requestRestore(new RestoreObserver() {
            @Override
            public void restoreStarting(int numPackages) {
                super.restoreStarting(numPackages);
            }
        });
    }
}
