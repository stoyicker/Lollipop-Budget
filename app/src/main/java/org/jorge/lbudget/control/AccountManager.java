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

package org.jorge.lbudget.control;

import org.jorge.lbudget.control.adapters.AccountListRecyclerAdapter;
import org.jorge.lbudget.io.db.SQLiteDAO;

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
        mAccountList = SQLiteDAO.getAccounts();
    }

    public List<AccountListRecyclerAdapter.AccountDataModel> getAccounts() {
        return mAccountList;
    }

    public Boolean addAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        Boolean ret = SQLiteDAO.addAccount(account) && !mAccountList.contains(account);
        if (!mAccountList.contains(account)) {
            mAccountList.add(account);
        }
        return ret;
    }

    public Boolean removeAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        return SQLiteDAO.removeAccount(account) && mAccountList.remove(account);
    }

    public Boolean setSelectedAccount(AccountListRecyclerAdapter.AccountDataModel newSelectedAccount) {
        return SQLiteDAO.setSelectedAccount(newSelectedAccount);
    }

    public AccountListRecyclerAdapter.AccountDataModel getSelectedAccount() {
        for (AccountListRecyclerAdapter.AccountDataModel acc : mAccountList) {
            if (acc.isSelected())
                return acc;
        }
        throw new IllegalStateException("No account is selected");
    }
}
