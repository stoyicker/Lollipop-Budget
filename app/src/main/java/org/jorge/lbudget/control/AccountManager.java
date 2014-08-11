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

import android.content.Context;
import android.os.AsyncTask;

import org.jorge.lbudget.io.files.FileManager;
import org.jorge.lbudget.io.files.XMLFileManager;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AccountManager {

    private List<AccountListRecyclerAdapter.AccountDataModel> mAccountList;
    private AccountListRecyclerAdapter.AccountDataModel selectedAccount;
    private static AccountManager singleton;
    private Context mContext;

    private AccountManager(Context _context) {
        mContext = _context;
    }

    public static AccountManager getInstance(Context _context) {
        if (singleton == null) {
            singleton = new AccountManager(_context);
        }
        return singleton;
    }

    public AccountListRecyclerAdapter.AccountDataModel getSelectedAccount() {
        if (selectedAccount == null)
            throw new IllegalStateException("No account selected.");
        return selectedAccount;
    }

    public void parseAccounts() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executor parserTaskExecutor = Executors.newSingleThreadExecutor();
        new AsyncTask<CountDownLatch, Void, Void>() {
            @Override
            protected Void doInBackground(CountDownLatch... countDownLatches) {
                mAccountList = new ArrayList<>();
                File accountsFile = new File(mContext.getExternalFilesDir(null) + LBudgetUtils.getString(mContext, "accounts_file_name"));
                if (!accountsFile.exists()) {
                    try {
                        if (!FileManager.writeStringToFile(LBudgetUtils.getString(mContext, "default_accounts_file_contents").replace("{DEFAULT_ACCOUNT_NAME}", LBudgetUtils.getString(mContext, "default_account_name")), accountsFile)) {
                            mAccountList.add(new AccountListRecyclerAdapter.AccountDataModel(1 + "", LBudgetUtils.getString(mContext, "default_account_name"), "USD"));
                            countDownLatches[0].countDown();
                            return null;
                        }
                    } catch (IOException e) {
                        //The exception is handled by assigning a  single, new, and temporary default account
                        mAccountList.add(new AccountListRecyclerAdapter.AccountDataModel(1 + "", LBudgetUtils.getString(mContext, "default_account_name"), "USD"));
                        countDownLatches[0].countDown();
                        return null;
                    }
                }
                List<String> ids = XMLFileManager.getAllOfType("account", "id", accountsFile);
                List<String> names = XMLFileManager.getAllOfType("account", "name", accountsFile);
                List<String> currencies = XMLFileManager.getAllOfType("account", "currency", accountsFile);
                List<String> selectedStates = XMLFileManager.getAllOfType("account", "selected", accountsFile);
                int length = ids.size();
                for (int i = 0; i < length; i++) {
                    AccountListRecyclerAdapter.AccountDataModel thisAccount = new AccountListRecyclerAdapter.AccountDataModel(ids.get(i), names.get(i), currencies.get(i));
                    mAccountList.add(thisAccount);
                    if (selectedStates.get(i).contentEquals("true")) {
                        selectedAccount = thisAccount;
                    }
                }
                countDownLatches[0].countDown();
                return null;
            }
        }.executeOnExecutor(parserTaskExecutor, countDownLatch);

        countDownLatch.countDown();
    }

    public List<AccountListRecyclerAdapter.AccountDataModel> getAccounts() {
        return mAccountList;
    }

    public Boolean setSelectedAccount(AccountListRecyclerAdapter.AccountDataModel newSelectedAccount) {
        File accountsFile = new File(mContext.getExternalFilesDir(null)+LBudgetUtils.getString(mContext, "accounts_file_name"));
        List<String[]> nonSelected = new ArrayList<>(), selected = new ArrayList<>();
        nonSelected.add(new String[]{"selected", "false"});
        selected.add(new String[]{"selected", "true"});
        try {
            XMLFileManager.updateNodeInfo("account", "id", getSelectedAccount().getId(), accountsFile, nonSelected);
            XMLFileManager.updateNodeInfo("account", "id", newSelectedAccount.getId(), accountsFile, selected);
        } catch (IOException e) {
            return Boolean.FALSE;
        }
        this.selectedAccount = newSelectedAccount;
        return Boolean.TRUE;
    }
}
