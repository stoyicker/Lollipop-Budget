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

package org.jorge.lbudget.ui.frags;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jorge.lbudget.R;
import org.jorge.lbudget.logic.adapters.AccountListRecyclerAdapter;
import org.jorge.lbudget.logic.controllers.AccountManager;
import org.jorge.lbudget.ui.utils.FloatingActionButton;
import org.jorge.lbudget.ui.utils.undobar.UndoBarShowStateListener;

public class AccountListFragment extends Fragment implements UndoBarShowStateListener {

    private RecyclerView mAccountsRecyclerView;
    private Context mContext;
    private FloatingActionButton newAccountButton;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAccountsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAccountsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        final AccountListRecyclerAdapter mAdapter;
        mAccountsRecyclerView.setAdapter(mAdapter = new AccountListRecyclerAdapter(this, getActivity(), AccountManager.getInstance().getAccounts(), mAccountsRecyclerView));
        newAccountButton = (FloatingActionButton) view.findViewById(R.id.button_new_account);
        newAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.createNewAccount();
            }
        });
        newAccountButton.attachToRecyclerView(mAccountsRecyclerView);
    }

    public void onShowUndoBar() {
        newAccountButton.setEnabled(Boolean.FALSE);
    }

    public void onHideUndoBar() {
        newAccountButton.setEnabled(Boolean.TRUE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_account_list, container, Boolean.FALSE);
        mAccountsRecyclerView = (RecyclerView) ret.findViewById(R.id.account_list_view);
        return ret;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
    }
}
