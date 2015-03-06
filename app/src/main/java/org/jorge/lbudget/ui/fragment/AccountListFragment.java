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

package org.jorge.lbudget.ui.fragment;

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
import android.view.inputmethod.InputMethodManager;

import com.hudomju.swipe.OnItemClickListener;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.SwipeableItemClickListener;
import com.hudomju.swipe.adapter.RecyclerViewAdapter;
import com.hudomju.swipe.adapter.ViewAdapter;

import org.jorge.lbudget.R;
import org.jorge.lbudget.controller.AccountManager;
import org.jorge.lbudget.ui.adapter.AccountListRecyclerAdapter;
import org.jorge.lbudget.ui.component.FloatingActionHideActionBarButton;

public class AccountListFragment extends Fragment {

    private RecyclerView mAccountsRecyclerView;
    private Context mContext;
    private FloatingActionHideActionBarButton mNewAccountButton;

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAccountsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAccountsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        final AccountListRecyclerAdapter adapter;
        mAccountsRecyclerView.setAdapter(adapter = new AccountListRecyclerAdapter(getActivity(),
                AccountManager.getInstance().getAccounts(), mAccountsRecyclerView));

        final SwipeToDismissTouchListener touchListener =
                new SwipeToDismissTouchListener<>(
                        new RecyclerViewAdapter(mAccountsRecyclerView),
                        new SwipeToDismissTouchListener.DismissCallbacks<ViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return !adapter.isSelected(position);
                            }

                            @Override
                            public void onDismiss(ViewAdapter viewAdapter, int i) {
                                if (i > -1 && i < adapter.getItemCount()) {
                                    adapter.runDestroy(i);
                                }
                                final InputMethodManager imm = (InputMethodManager) mContext
                                        .getSystemService(
                                                Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(mAccountsRecyclerView.getWindowToken
                                        (), 0);
                                //Just limit to deal with one, in the future we'll see what happens
                                // with many
                            }
                        });
        mAccountsRecyclerView.setOnTouchListener(touchListener);
        mAccountsRecyclerView.setOnScrollListener((RecyclerView.OnScrollListener) touchListener
                .makeScrollListener());
        mAccountsRecyclerView.addOnItemTouchListener(new SwipeableItemClickListener(mContext,
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final Integer viewId = view.getId();
                        if (viewId == R.id.txt_delete) {
                            touchListener.processPendingDismisses();
                        } else if (viewId == R.id.txt_undo) {
                            touchListener.undoPendingDismiss();
                            mAccountsRecyclerView.smoothScrollToPosition(position);
                        } else {
                            adapter.performClick(view, position);
                        }
                    }
                }
        ));
        mNewAccountButton = (FloatingActionHideActionBarButton) view.findViewById(R.id
                .button_new_item);
        mNewAccountButton.setOnClickListener(new View.OnClickListener()

                                             {
                                                 @Override
                                                 public void onClick(View view) {
                                                     adapter.createNewAccount();
                                                 }
                                             }

        );
        mNewAccountButton.attachToRecyclerView(mAccountsRecyclerView);
    }

    public void onShowUndoBar() {
        mNewAccountButton.setEnabled(Boolean.FALSE);
    }

    public void onHideUndoBar() {
        mNewAccountButton.setEnabled(Boolean.TRUE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_account_list, container, Boolean.FALSE);
        mAccountsRecyclerView = (RecyclerView) ret.findViewById(R.id.account_list_view);
        FloatingActionHideActionBarButton newItemButton = (FloatingActionHideActionBarButton) ret
                .findViewById(R.id.button_new_item);
        newItemButton.setTopPadding(mAccountsRecyclerView.getPaddingTop());
        newItemButton.setActivity(getActivity());
        return ret;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
    }
}
