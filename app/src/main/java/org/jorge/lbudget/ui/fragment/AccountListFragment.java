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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.jorge.lbudget.R;
import org.jorge.lbudget.controller.AccountManager;
import org.jorge.lbudget.ui.adapter.AccountListRecyclerAdapter;
import org.jorge.lbudget.ui.component.SwipeDismissRecyclerViewTouchListener;
import org.jorge.lbudget.ui.util.FloatingActionHideActionBarButton;
import org.jorge.lbudget.ui.util.undobar.UndoBarShowStateListener;

public class AccountListFragment extends Fragment implements UndoBarShowStateListener {

    private RecyclerView mAccountsRecyclerView;
    private Context mContext;
    private FloatingActionHideActionBarButton mNewAccountButton;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAccountsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAccountsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        final AccountListRecyclerAdapter mAdapter;
        mAccountsRecyclerView.setAdapter(mAdapter = new AccountListRecyclerAdapter(this,
                getActivity(), AccountManager.getInstance().getAccounts(), mAccountsRecyclerView));

        SwipeDismissRecyclerViewTouchListener touchListener =
                new SwipeDismissRecyclerViewTouchListener(
                        mAccountsRecyclerView,
                        new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return !mAdapter.isSelected(position);
                            }

                            @Override
                            public void onDismiss(RecyclerView recyclerView,
                                                  int[] reverseSortedPositions) {
                                if (reverseSortedPositions != null)
                                    mAdapter.runDestroy(recyclerView,
                                            reverseSortedPositions[0]); //Just limit
                                // to deal with one, in the future we'll see what happens
                                // with many
                            }
                        });
        mAccountsRecyclerView.setOnTouchListener(touchListener);
        mAccountsRecyclerView.setOnScrollListener(touchListener.makeScrollListener());
        mAccountsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mContext,
                new SwipeDismissRecyclerViewTouchListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mAdapter.runClick(view, position);
                    }
                }));
        mNewAccountButton = (FloatingActionHideActionBarButton) view.findViewById(R.id
                .button_new_item);
        mNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.createNewAccount();
            }
        });
        mNewAccountButton.attachToRecyclerView(mAccountsRecyclerView);
    }

    private class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private SwipeDismissRecyclerViewTouchListener.OnItemClickListener mListener;

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, SwipeDismissRecyclerViewTouchListener
                .OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return Boolean.TRUE;
                        }
                    });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildPosition(childView));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }
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
