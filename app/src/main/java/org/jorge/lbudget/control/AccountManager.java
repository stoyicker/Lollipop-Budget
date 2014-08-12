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
import android.util.Log;

import org.jorge.lbudget.io.files.FileManager;
import org.jorge.lbudget.io.files.XMLFileManager;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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

    public void parseAccounts(final Boolean addDefaultAccount) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executor parserTaskExecutor = Executors.newSingleThreadExecutor();
        new AsyncTask<CountDownLatch, Void, Void>() {
            @Override
            protected Void doInBackground(CountDownLatch... countDownLatches) {
                mAccountList = new ArrayList<>();
                File accountsFile = new File(mContext.getExternalFilesDir(null) + LBudgetUtils.getString(mContext, "accounts_file_name"));
                if (!accountsFile.exists()) {
                    try {
                        if (!FileManager.writeStringToFile(LBudgetUtils.getString(mContext, "default_xml_file_contents"), accountsFile)) {
                            if (addDefaultAccount) {//TODO Add default account to mAccountList ONLY
                                mAccountList.add(new AccountListRecyclerAdapter.AccountDataModel(LBudgetUtils.getString(mContext, "default_account_id"), LBudgetUtils.getString(mContext, "default_account_name"), LBudgetUtils.getString(mContext, "default_account_currency")));
                            }
                            countDownLatches[0].countDown();
                            return null;
                        } else if (addDefaultAccount) {
                            //TODO Add default account to file instead
                        }
                    } catch (IOException e) {
                        //The exception is handled by assigning a  single, new, and temporary default account
                        if (addDefaultAccount)
                            mAccountList.add(new AccountListRecyclerAdapter.AccountDataModel(LBudgetUtils.getString(mContext, "default_account_id"), LBudgetUtils.getString(mContext, "default_account_name"), LBudgetUtils.getString(mContext, "default_account_currency")));
                        countDownLatches[0].countDown();
                        return null;
                    }
                }
                final String accountNodeName = LBudgetUtils.getString(mContext, "account_node_name"), idAttr = LBudgetUtils.getString(mContext, "attribute_id_name"), nameAttr = LBudgetUtils.getString(mContext, "attribute_name_name"), currencyAttr = LBudgetUtils.getString(mContext, "attribute_currency_name"), selectedAttr = LBudgetUtils.getString(mContext, "attribute_selected_name");
                List<String> ids = XMLFileManager.getAllOfType(accountNodeName, idAttr, accountsFile);
                List<String> names = XMLFileManager.getAllOfType(accountNodeName, nameAttr, accountsFile);
                List<String> currencies = XMLFileManager.getAllOfType(accountNodeName, currencyAttr, accountsFile);
                List<String> selectedStates = XMLFileManager.getAllOfType(accountNodeName, selectedAttr, accountsFile);
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
        File accountsFile = new File(mContext.getExternalFilesDir(null) + LBudgetUtils.getString(mContext, "accounts_file_name"));
        List<String[]> nonSelected = new ArrayList<>(), selected = new ArrayList<>();
        final String accountNodeName = LBudgetUtils.getString(mContext, "account_node_name"), idAttr = LBudgetUtils.getString(mContext, "attribute_id_name"), selectedAttr = LBudgetUtils.getString(mContext, "attribute_selected_name");
        nonSelected.add(new String[]{selectedAttr, "false"});
        selected.add(new String[]{selectedAttr, "true"});
        if (!accountsFile.exists()) {
            parseAccounts(Boolean.FALSE);
            addAccount(newSelectedAccount);
            return Boolean.TRUE;
        }
        //TODO If file does exist but it has error --> Remove it, create new one and add this account
        //TODO If file does exist but account does not --> Add the account
        try {
            XMLFileManager.updateNodeInfo(accountNodeName, idAttr, getSelectedAccount().getId(), accountsFile, nonSelected);
            XMLFileManager.updateNodeInfo(accountNodeName, idAttr, newSelectedAccount.getId(), accountsFile, selected);
        } catch (IOException e) {
            return Boolean.FALSE;
        }
        this.selectedAccount = newSelectedAccount;
        return Boolean.TRUE;
    }

    private void addAccount(AccountListRecyclerAdapter.AccountDataModel account) {
        File accountsFile = new File(mContext.getExternalFilesDir(null) + LBudgetUtils.getString(mContext, "accounts_file_name"));
        mAccountList.add(account);
        final String accountNodeName = LBudgetUtils.getString(mContext, "account_node_name"), idAttr = LBudgetUtils.getString(mContext, "attribute_id_name"), nameAttr = LBudgetUtils.getString(mContext, "attribute_name_name"), currencyAttr = LBudgetUtils.getString(mContext, "attribute_currency_name"), selectedAttr = LBudgetUtils.getString(mContext, "attribute_selected_name");
        List<String[]> attributes = new LinkedList<>();
        attributes.add(new String[]{nameAttr, account.getAccountName()});
        attributes.add(new String[]{currencyAttr, account.getAccountCurrency()});
        attributes.add(new String[]{selectedAttr, "" + selectedAccount.equals(account)});
        try {
            XMLFileManager.addNode(accountNodeName, idAttr, account.getId(), accountsFile, attributes);
        } catch (IOException e) {
            //All checks should be performed earlier and, if something happens here, the account should not be added
            Log.e("debug", Arrays.toString(e.getStackTrace()));
        }
    }
}
