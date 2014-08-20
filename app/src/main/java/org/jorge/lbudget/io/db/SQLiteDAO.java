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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import org.jorge.lbudget.R;
import org.jorge.lbudget.io.net.LBackupAgent;
import org.jorge.lbudget.logic.adapters.AccountListRecyclerAdapter;
import org.jorge.lbudget.logic.adapters.MovementListRecyclerAdapter;
import org.jorge.lbudget.logic.controllers.AccountManager;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SQLiteDAO extends RobustSQLiteOpenHelper {

    public static final Object[] DB_LOCK = new Object[0];
    private final String ACCOUNTS_TABLE_NAME, ACCOUNT_KEY_ID, ACCOUNT_KEY_NAME, ACCOUNT_KEY_SELECTED, MOVEMENT_KEY_ID, MOVEMENT_KEY_TITLE, MOVEMENT_KEY_AMOUNT;
    private static Context mContext;
    private static SQLiteDAO singleton;
    private static Executor BACKGROUND_OPS_EXECUTOR = Executors.newSingleThreadExecutor();

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

    public static void setup(Context _context) {
        if (singleton == null) {
            singleton = new SQLiteDAO(_context);
            mContext = _context;
        }
    }

    public static SQLiteDAO getInstance() {
        if (singleton == null)
            throw new IllegalStateException("SQLiteDAO.setup(Context) must be called before trying to retrieve the instance.");
        return singleton;
    }

    public List<AccountListRecyclerAdapter.AccountDataModel> getAccounts() {
        List<AccountListRecyclerAdapter.AccountDataModel> ret;
        SQLiteDatabase db = getReadableDatabase();
        synchronized (DB_LOCK) {
            db.beginTransaction();
            Cursor allAccounts = db.query(ACCOUNTS_TABLE_NAME, null, null, null, null, null, ACCOUNT_KEY_ID + " ASC");
            ret = new ArrayList<>();
            if (allAccounts != null && allAccounts.getCount() > 0) {
                if (allAccounts.moveToFirst()) {
                    do {
                        ret.add(mapStorableToAccount(allAccounts));
                        allAccounts.moveToNext();
                    } while (allAccounts.isLast());
                }
            }
            if (allAccounts != null)
                allAccounts.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return ret;
    }

    public List<MovementListRecyclerAdapter.MovementDataModel> loadAccountMovements() {
        List<MovementListRecyclerAdapter.MovementDataModel> ret;
        SQLiteDatabase db = getReadableDatabase();
        final String selectedAccMovTableName = LBudgetUtils.getString(mContext, "account_table_name_prefix") + AccountManager.getInstance().getSelectedAccount().getAccountId();
        synchronized (DB_LOCK) {
            db.beginTransaction();
            Cursor accMovements = db.query(selectedAccMovTableName, null, null, null, null, null, MOVEMENT_KEY_ID + " ASC");
            ret = new ArrayList<>();
            if (accMovements != null && accMovements.getCount() > 0) {
                if (accMovements.moveToFirst()) {
                    do {
                        ret.add(mapStorableToMovement(accMovements));
                        accMovements.moveToNext();
                    } while (accMovements.isLast());
                }
            }
            if (accMovements != null)
                accMovements.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return ret;
    }

    public Boolean addAccount(final AccountListRecyclerAdapter.AccountDataModel account) {
        new AsyncTask<AccountListRecyclerAdapter.AccountDataModel, Void, Void>() {
            @Override
            protected Void doInBackground(AccountListRecyclerAdapter.AccountDataModel... accounts) {
                if (accounts[0] == null)
                    throw new IllegalArgumentException("A null account cannot be added to the database.");
                SQLiteDatabase db = getWritableDatabase();
                synchronized (DB_LOCK) {
                    db.beginTransaction();
                    db.insert(ACCOUNTS_TABLE_NAME, null, mapAccountToStorable(accounts[0]));
                    createAccountTable(getWritableDatabase(), accounts[0].getAccountId());
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
                return null;
            }
        }.executeOnExecutor(BACKGROUND_OPS_EXECUTOR, account);
        return Boolean.TRUE;
    }

    public Boolean removeAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        new AsyncTask<AccountListRecyclerAdapter.AccountDataModel, Void, Void>() {
            @Override
            protected Void doInBackground(AccountListRecyclerAdapter.AccountDataModel... accounts) {
                if (accounts[0] == null)
                    throw new IllegalArgumentException("A null account cannot be removed from the database.");
                SQLiteDatabase db = getWritableDatabase();
                synchronized (DB_LOCK) {
                    db.beginTransaction();
                    db.delete(ACCOUNTS_TABLE_NAME, ACCOUNT_KEY_ID + " = " + accounts[0].getAccountId(), null);
                    deleteAccountTable(getWritableDatabase(), accounts[0].getAccountId());
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
                return null;
            }
        }.executeOnExecutor(BACKGROUND_OPS_EXECUTOR, account);
        return Boolean.TRUE;
    }

    public Boolean setSelectedAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        new AsyncTask<AccountListRecyclerAdapter.AccountDataModel, Void, Void>() {
            @Override
            protected Void doInBackground(AccountListRecyclerAdapter.AccountDataModel... accounts) {
                if (accounts[0] == null)
                    throw new IllegalArgumentException("A null account cannot be set as the selected one in the database.");
                SQLiteDatabase db = getWritableDatabase();
                ContentValues selectedCell = new ContentValues(), unselectedCell = new ContentValues();
                selectedCell.put(ACCOUNT_KEY_SELECTED, 1);
                unselectedCell.put(ACCOUNT_KEY_SELECTED, 0);
                synchronized (DB_LOCK) {
                    db.beginTransaction();
                    db.update(ACCOUNTS_TABLE_NAME, unselectedCell, ACCOUNT_KEY_SELECTED + " = " + 1, null);
                    db.update(ACCOUNTS_TABLE_NAME, selectedCell, ACCOUNT_KEY_ID + " = " + accounts[0].getAccountId(), null);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
                return null;
            }
        }.executeOnExecutor(BACKGROUND_OPS_EXECUTOR, account);
        return Boolean.TRUE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        synchronized (DB_LOCK) {
            db.beginTransaction();
            final String createAccTableCmd = ("CREATE TABLE IF NOT EXISTS " + ACCOUNTS_TABLE_NAME + " ( " +
                    ACCOUNT_KEY_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE ASC AUTOINCREMENT, " +
                    ACCOUNT_KEY_NAME + " TEXT UNIQUE ON CONFLICT IGNORE NOT NULL ON CONFLICT IGNORE, " +
                    ACCOUNT_KEY_SELECTED + " INTEGER NOT NULL ON CONFLICT IGNORE " +
                    "CHECK ((" + ACCOUNT_KEY_SELECTED + " = 0 OR " + ACCOUNT_KEY_SELECTED + " = 1) AND (SELECT SUM(" + ACCOUNT_KEY_SELECTED + ") = 1)) ON CONFLICT IGNORE" +
                    " ) ").toUpperCase(Locale.ENGLISH);
            db.execSQL(createAccTableCmd);
            addTableName(ACCOUNTS_TABLE_NAME);
            AccountListRecyclerAdapter.AccountDataModel defaultAccDataModel;
            ContentValues defaultAcc = mapAccountToStorable(defaultAccDataModel = new AccountListRecyclerAdapter.AccountDataModel(LBudgetUtils.getInt(mContext, "default_account_id"), LBudgetUtils.getString(mContext, "default_account_name"), mContext.getResources().getBoolean(R.bool.default_account_selected)));
            db.insert(ACCOUNTS_TABLE_NAME, null, defaultAcc);
            createAccountTable(db, defaultAccDataModel.getAccountId());
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        LBackupAgent.requestBackup(mContext);
    }

    private ContentValues mapAccountToStorable(AccountListRecyclerAdapter.AccountDataModel account) {
        ContentValues ret = new ContentValues();
        ret.put(ACCOUNT_KEY_ID, account.getAccountId());
        ret.put(ACCOUNT_KEY_NAME, account.getAccountName());
        ret.put(ACCOUNT_KEY_SELECTED, account.isSelected() ? 1 : 0);
        return ret;
    }

    private AccountListRecyclerAdapter.AccountDataModel mapStorableToAccount(Cursor cursor) {
        return new AccountListRecyclerAdapter.AccountDataModel(cursor.getInt(0), cursor.getString(1), cursor.getInt(2) > 0 ? Boolean.TRUE : Boolean.FALSE);
    }

    private ContentValues mapMovementToStorable(MovementListRecyclerAdapter.MovementDataModel movement) {
        ContentValues ret = new ContentValues();
        ret.put(MOVEMENT_KEY_ID, movement.getMovementId());
        ret.put(MOVEMENT_KEY_TITLE, movement.getMovementTitle());
        ret.put(MOVEMENT_KEY_AMOUNT, movement.getMovementAmount());
        return ret;
    }

    private MovementListRecyclerAdapter.MovementDataModel mapStorableToMovement(Cursor cursor) {
        return new MovementListRecyclerAdapter.MovementDataModel(cursor.getInt(0), cursor.getString(1), cursor.getLong(2));
    }

    /**
     * Must be called with the database ready to be written, an on-going transaction and the lock obtained.
     *
     * @param db        The database where the new table must be created.
     * @param accountId The id of the account that the new table is going to be associated to.
     */
    private void createAccountTable(SQLiteDatabase db, int accountId) {
        final String accountTableName = LBudgetUtils.getString(mContext, "account_table_name_prefix") + accountId;
        final String createAccMovTableCmd = ("CREATE TABLE IF NOT EXISTS " + accountTableName + " ( " +
                MOVEMENT_KEY_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE ASC AUTOINCREMENT, " +
                MOVEMENT_KEY_TITLE + " TEXT, " +
                MOVEMENT_KEY_AMOUNT + " INTEGER NOT NULL ON CONFLICT IGNORE " +
                "CHECK ((" + MOVEMENT_KEY_AMOUNT + " <> 0 ON CONFLICT IGNORE" +
                " ) ").toUpperCase(Locale.ENGLISH);
        db.execSQL(createAccMovTableCmd);
        addTableName(accountTableName);
        //Set the initial index
        db.insert(ACCOUNTS_TABLE_NAME, null, mapMovementToStorable(new MovementListRecyclerAdapter.MovementDataModel(LBudgetUtils.getInt(mContext, "default_movement_id") - 1, "", -1)));
        db.delete(ACCOUNTS_TABLE_NAME, null, null);
    }

    /**
     * Must be called with the database ready to be written, an on-going transaction and the lock obtained.
     *
     * @param db        The database where the new table must be created.
     * @param accountId The id of the account that the new table is going to be associated to.
     */
    private void deleteAccountTable(SQLiteDatabase db, int accountId) {
        final String accountTableName = LBudgetUtils.getString(mContext, "account_table_name_prefix") + accountId;
        final String deleteAccMovTableCmd = ("DROP TABLE IF EXISTS " + accountTableName).toUpperCase(Locale.ENGLISH);
        db.execSQL(deleteAccMovTableCmd);
        removeTableName(accountTableName);
    }

    public void setAccountName(String id, String newName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues newNameContainer = new ContentValues();
        newNameContainer.put(ACCOUNT_KEY_NAME, newName);
        synchronized (DB_LOCK) {
            db.beginTransaction();
            db.update(ACCOUNTS_TABLE_NAME, newNameContainer, ACCOUNT_KEY_ID + " = " + id, null);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }
}
