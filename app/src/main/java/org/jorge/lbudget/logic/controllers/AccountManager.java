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

package org.jorge.lbudget.logic.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jorge.lbudget.io.db.SQLiteDAO;
import org.jorge.lbudget.logic.adapters.AccountListRecyclerAdapter;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.List;

public class AccountManager {

    private List<AccountListRecyclerAdapter.AccountDataModel> mAccountList;
    private static AccountManager singleton;

    private AccountManager() {
    }

    public static AccountManager getInstance() {
        if (singleton == null) {
            singleton = new AccountManager();
        }
        return singleton;
    }

    public void setup() {
        mAccountList = SQLiteDAO.getInstance().getAccounts();
    }

    public List<AccountListRecyclerAdapter.AccountDataModel> getAccounts() {
        return mAccountList;
    }

    public Boolean addAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        Boolean ret = SQLiteDAO.getInstance().addAccount(account) && !mAccountList.contains(account);
        if (!mAccountList.contains(account)) {
            mAccountList.add(account);
        }
        return ret;
    }

    public Boolean removeAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        return SQLiteDAO.getInstance().removeAccount(account) && mAccountList.remove(account);
    }

    public Boolean setSelectedAccount(AccountListRecyclerAdapter.AccountDataModel newSelectedAccount) {
        return SQLiteDAO.getInstance().setSelectedAccount(newSelectedAccount);
    }

    public AccountListRecyclerAdapter.AccountDataModel getSelectedAccount() {
        for (AccountListRecyclerAdapter.AccountDataModel acc : mAccountList) {
            if (acc.isSelected())
                return acc;
        }
        throw new IllegalStateException("No account is selected");
    }

    public String getSelectedCurrency(Context _context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
        return preferences.getString(LBudgetUtils.getString(_context, "pref_key_currency_code"), LBudgetUtils.getString(_context, "currency_172"));
    }

    public void setAccountName(int id, String newName) {
        SQLiteDAO.getInstance().setAccountName(id, newName);
    }
}
