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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.jorge.lbudget.R;
import org.jorge.lbudget.control.adapters.AccountListRecyclerAdapter;
import org.jorge.lbudget.control.adapters.MovementListRecyclerAdapter;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SQLiteDAO extends RobustSQLiteOpenHelper {

    public static final Object[] DB_LOCK = new Object[0];
    private final String ACCOUNTS_TABLE_NAME, ACCOUNT_KEY_ID, ACCOUNT_KEY_NAME, ACCOUNT_KEY_SELECTED, MOVEMENT_KEY_ID, MOVEMENT_KEY_TITLE, MOVEMENT_KEY_AMOUNT;
    private static Context mContext;
    private static SQLiteDAO singleton;

    private SQLiteDAO(Context _context) {
        super(_context, LBudgetUtils.getString(_context, "db_name"), null, LBudgetUtils.getInt(_context, "db_version"));
        mContext = _context;
        ACCOUNTS_TABLE_NAME = LBudgetUtils.getString(mContext, "accounts_table_name").toUpperCase(Locale.ENGLISH);
        ACCOUNT_KEY_ID = LBudgetUtils.getString(mContext, "account_key_id");
        ACCOUNT_KEY_NAME = LBudgetUtils.getString(mContext, "account_key_name");
        ACCOUNT_KEY_SELECTED = LBudgetUtils.getString(mContext, "account_key_selected");
        MOVEMENT_KEY_ID = LBudgetUtils.getString(mContext, "movement_key_id");
        MOVEMENT_KEY_TITLE = LBudgetUtils.getString(mContext, "movement_key_title");
        MOVEMENT_KEY_AMOUNT = LBudgetUtils.getString(mContext, "movement_key_amount");
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
        //TODO Return the list, sorted by id. Fix the table if there are none or more than one selected accounts, or if there are duplicate names
        List<AccountListRecyclerAdapter.AccountDataModel> stubRet = new ArrayList<>();
        stubRet.add(new AccountListRecyclerAdapter.AccountDataModel(1, "cuentanombre", Boolean.TRUE));
        stubRet.add(new AccountListRecyclerAdapter.AccountDataModel(2, "cuentanoseleccionada", Boolean.FALSE));
        stubRet.add(new AccountListRecyclerAdapter.AccountDataModel(3, "terceracuenta", Boolean.FALSE));
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
        super.onCreate(db);
        synchronized (DB_LOCK) {
            db.beginTransaction();
            final String createAccTableCmd = "CREATE TABLE IF NOT EXISTS " + ACCOUNTS_TABLE_NAME + " ( " +
                    ACCOUNT_KEY_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE ASC AUTOINCREMENT, " +
                    ACCOUNT_KEY_NAME + " TEXT UNIQUE ON CONFLICT IGNORE NOT NULL ON CONFLICT IGNORE, " +
                    ACCOUNT_KEY_SELECTED + " INTEGER NOT NULL ON CONFLICT IGNORE " +
                    "CHECK ((" + ACCOUNT_KEY_SELECTED + " = 0 OR " + ACCOUNT_KEY_SELECTED + " = 1) AND (SELECT SUM(" + ACCOUNT_KEY_SELECTED + ") = 1)) ON CONFLICT IGNORE" +
                    " ) ".toUpperCase(Locale.ENGLISH);
            db.execSQL(createAccTableCmd);
            addTableName(ACCOUNTS_TABLE_NAME);
            AccountListRecyclerAdapter.AccountDataModel defaultAccDataModel;
            ContentValues defaultAcc = mapAccountToStorable(defaultAccDataModel = new AccountListRecyclerAdapter.AccountDataModel(LBudgetUtils.getInt(mContext, "default_account_id"), LBudgetUtils.getString(mContext, "default_account_name"), mContext.getResources().getBoolean(R.bool.default_account_selected)));
            db.insert(ACCOUNTS_TABLE_NAME, null, defaultAcc);
            createAccountTable(db, defaultAccDataModel.getAccountId());
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    private ContentValues mapAccountToStorable(AccountListRecyclerAdapter.AccountDataModel account) {
        ContentValues ret = new ContentValues();
        ret.put(ACCOUNT_KEY_ID, account.getAccountId());
        ret.put(ACCOUNT_KEY_NAME, account.getAccountName());
        ret.put(ACCOUNT_KEY_SELECTED, account.isSelected() ? 1 : 0);
        return ret;
    }

    private ContentValues mapMovementToStorable(MovementListRecyclerAdapter.MovementDataModel movement) {
        ContentValues ret = new ContentValues();
        ret.put(ACCOUNT_KEY_ID, movement.getMovementId());
        ret.put(ACCOUNT_KEY_NAME, movement.getMovementAmount());
        ret.put(ACCOUNT_KEY_SELECTED, movement.getMovementTitle());
        return ret;
    }

    /**
     * Must be called with the database ready to be written, an on-going transaction and the lock obtained.
     *
     * @param db        The database where the new table must be created.
     * @param accountId The id of the account that the new table is going to be associated to.
     */
    private void createAccountTable(SQLiteDatabase db, int accountId) {
        final String accountTableName = LBudgetUtils.getString(mContext, "account_table_name_prefix") + accountId;
        final String createAccMovTableCmd = "CREATE TABLE IF NOT EXISTS " + accountTableName + " ( " +
                MOVEMENT_KEY_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE ASC AUTOINCREMENT, " +
                MOVEMENT_KEY_TITLE + " TEXT, " +
                MOVEMENT_KEY_AMOUNT + " INTEGER NOT NULL ON CONFLICT IGNORE " +
                "CHECK ((" + MOVEMENT_KEY_AMOUNT + " <> 0 ON CONFLICT IGNORE" +
                " ) ".toUpperCase(Locale.ENGLISH);
        db.execSQL(createAccMovTableCmd);
        addTableName(accountTableName);
        //Set the initial index
        db.insert(ACCOUNTS_TABLE_NAME, null, mapMovementToStorable(new MovementListRecyclerAdapter.MovementDataModel(LBudgetUtils.getInt(mContext, "default_movement_id") - 1, "", -1)));
        db.delete(ACCOUNTS_TABLE_NAME, null, null);
    }
}
