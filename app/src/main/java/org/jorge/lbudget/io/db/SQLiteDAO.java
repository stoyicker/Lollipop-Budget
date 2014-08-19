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

import org.jorge.lbudget.control.AccountManager;
import org.jorge.lbudget.control.adapters.AccountListRecyclerAdapter;
import org.jorge.lbudget.control.adapters.MovementListRecyclerAdapter;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.ArrayList;
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
        List<MovementListRecyclerAdapter.MovementDataModel> ret = new ArrayList<>();
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(1, "info1", 0));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(2, "info2", -123));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(3, "info3", 2));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(4, "info4", -30));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(5, "info5", 40));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(6, "info6", 500));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(7, "info7", 7000));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(8, "info8", -0));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(9, "info9", -80));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(10, "info10", 123));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(11, "info11", 598));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(12, "info12", 250));
        ret.add(new MovementListRecyclerAdapter.MovementDataModel(0, "info13", 145));
        return ret;
    }

    public static void setup(Context _context) {
        if (singleton == null) {
            singleton = new SQLiteDAO(_context);
            mContext = _context;
        }
    }

    public static List<AccountListRecyclerAdapter.AccountDataModel> getAccounts() {
        //TODO Return the list, sorted by id. Fix the table if there are none or more than one selected accounts
        List<AccountListRecyclerAdapter.AccountDataModel> stubRet = new ArrayList<>();
        stubRet.add(new AccountListRecyclerAdapter.AccountDataModel(1 + "", "cuentanombre", Boolean.TRUE));
        stubRet.add(new AccountListRecyclerAdapter.AccountDataModel(2 + "", "cuentanoseleccionada", Boolean.FALSE));
        stubRet.add(new AccountListRecyclerAdapter.AccountDataModel(3 + "", "terceracuenta", Boolean.FALSE));
        return stubRet;
    }

    public static Boolean addAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        //TODO addAccount
        return Boolean.TRUE;
    }

    public static Boolean removeAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        //TODO removeAccount
        return Boolean.TRUE;
    }

    public static Boolean setSelectedAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        //TODO setSelectedAccount
        return Boolean.TRUE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }
}
