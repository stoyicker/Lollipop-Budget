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

package org.jorge.lbudget.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.jorge.lbudget.logic.controllers.AccountManager;
import org.jorge.lbudget.io.db.SQLiteDAO;
import org.jorge.lbudget.io.files.FileManager;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.io.File;

public class InitialActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context appContext = getApplicationContext();
        SQLiteDAO.setup(appContext);
        flushCacheIfNecessary(appContext);
        AccountManager.getInstance().setup();
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        finishAfterTransition();
        startActivity(intent);
    }

    private void flushCacheIfNecessary(Context _context) {
        File cacheDir;
        int CACHE_SIZE_LIMIT_BYTES = LBudgetUtils.getInt(_context, "cache_size_limit_bytes");
        if ((cacheDir = _context.getCacheDir()).length() > CACHE_SIZE_LIMIT_BYTES) {
            FileManager.recursiveDelete(cacheDir);
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
        startActivity(new Intent(getApplicationContext(), InitialActivity.class));
    }
}
