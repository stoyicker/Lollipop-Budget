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

package org.jorge.lbudget.io.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.jorge.lbudget.control.MovementListRecyclerAdapter;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.List;

public class SQLiteDAO extends RobustSQLiteOpenHelper {

    public static final Object[] DB_LOCK = new Object[0];//This has to be <@string/app_name>+_DB
    private static Context mContext;
    private static SQLiteDAO singleton;

    private SQLiteDAO(Context _context) {
        super(_context, LBudgetUtils.getString(_context, "db_name"), null, LBudgetUtils.getInt(_context, "db_version"));
        mContext = _context;
    }

    @Override
    public void onRobustUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) throws SQLiteException {
        //No older versions have been released.
    }

    public static List<MovementListRecyclerAdapter.MovementDataModel> loadAccountMovements() {
        //TODO loadAccountMovements()
        return null;
    }

    public static void setup(Context _context) {
        if (singleton == null) {
            singleton = new SQLiteDAO(_context);
            mContext = _context;
        }
    }
}
