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

package org.jorge.lbudget.control.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import org.jorge.lbudget.R;
import org.jorge.lbudget.control.AccountManager;
import org.jorge.lbudget.io.net.LBackupAgent;
import org.jorge.lbudget.ui.utils.undobar.UndoBar;
import org.jorge.lbudget.utils.IMECloseListenableEditText;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.List;

public class AccountListRecyclerAdapter extends RecyclerView.Adapter<AccountListRecyclerAdapter.ViewHolder> {

    private final float MIN_SWIPE_WIDTH_PIXELS;
    private final Activity mActivity;
    private final Context mContext;
    private final RecyclerView mRecyclerView;
    private final List<AccountDataModel> items;
    @SuppressWarnings("FieldCanBeLocal")
    private final int itemLayout = R.layout.list_item_account_list;
    private static float x = Float.MAX_VALUE;

    public AccountListRecyclerAdapter(Activity activity, List<AccountDataModel> accounts, RecyclerView _recyclerView) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        items = accounts;
        mRecyclerView = _recyclerView;
        MIN_SWIPE_WIDTH_PIXELS = LBudgetUtils.getInt(mContext, "min_swipe_width_pixels");
    }

    public void add(AccountDataModel item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
        AccountManager.getInstance().addAccount(item);
        LBackupAgent.requestBackup(mContext);
    }

    public AccountDataModel remove(int position) {
        AccountDataModel ret = items.remove(position);
        notifyItemRemoved(position);
        AccountManager.getInstance().removeAccount(ret);
        LBackupAgent.requestBackup(mContext);
        return ret;
    }

    private void setSelectedAccount(int position) {
        AccountDataModel oldSelectedAccount = AccountManager.getInstance().getSelectedAccount(), newSelectedAccount = items.get(position);
        oldSelectedAccount.setSelected(Boolean.FALSE);
        newSelectedAccount.setSelected(Boolean.TRUE);
        int oldSelected = items.indexOf(oldSelectedAccount);
        AccountManager.getInstance().setSelectedAccount(newSelectedAccount);
        notifyItemChanged(position);
        notifyItemChanged(oldSelected);
        LBackupAgent.requestBackup(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, viewGroup, Boolean.FALSE));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        AccountDataModel item = items.get(i);
        viewHolder.accountNameView.setText(item.getAccountName());
        if (item.isSelected()) {
            viewHolder.wholeView.setBackgroundResource(R.color.selected_card_background);
            viewHolder.accountNameView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_edit_selected, 0, 0, 0);
            viewHolder.accountNameView.setTextAppearance(mContext, R.style.AccountNameTextSelected);
            viewHolder.accountNameView.setFocusable(Boolean.FALSE);
            viewHolder.accountNameView.setFocusableInTouchMode(Boolean.FALSE);
        } else {
            viewHolder.wholeView.setBackgroundResource(R.color.non_selected_card_background);
            viewHolder.accountNameView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_edit_non_selected, 0, 0, 0);
            viewHolder.accountNameView.setTextAppearance(mContext, R.style.AccountNameTextNonSelected);
            viewHolder.accountNameView.setFocusable(Boolean.TRUE);
            viewHolder.accountNameView.setFocusableInTouchMode(Boolean.TRUE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final IMECloseListenableEditText accountNameView;
        private final View wholeView;

        public ViewHolder(View itemView) {
            super(itemView);
            wholeView = itemView;
            View.OnTouchListener listener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            final float diff = motionEvent.getX() - x;
                            if (Math.abs(diff) >= MIN_SWIPE_WIDTH_PIXELS) {
                                if (!items.get(getPosition()).isSelected()) {
                                    final Animation fadeOut = AnimationUtils.loadAnimation(mContext, diff < 0 ? R.anim.fade_out_to_left : R.anim.fade_out_to_right);
                                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {
                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            wholeView.setVisibility(View.GONE);
                                            int pos;
                                            final AccountDataModel movement = items.remove(pos = getPosition());
                                            notifyItemRemoved(pos);
                                            new UndoBar.Builder(mActivity)
                                                    .setMessage(LBudgetUtils.getString(mContext, "movement_list_item_removal"))
                                                    .setListener(new UndoBar.Listener() {
                                                        @Override
                                                        public void onHide() {
                                                            AccountManager.getInstance().removeAccount(movement);
                                                            LBackupAgent.requestBackup(mContext);
                                                        }

                                                        @Override
                                                        public void onUndo(Parcelable token) {
                                                            int pos;
                                                            items.add(pos = getPosition(), movement);
                                                            notifyItemInserted(pos);
                                                            mRecyclerView.smoothScrollToPosition(pos);
                                                        }
                                                    })
                                                    .show();
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {
                                        }
                                    });
                                    wholeView.startAnimation(fadeOut);
                                }
                                return Boolean.TRUE;
                            } else {
                                x = Float.MAX_VALUE; //Reset x
                                mActivity.findViewById(android.R.id.content).requestFocus();
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                setSelectedAccount(getPosition());
                                return Boolean.TRUE;
                            }
                        case MotionEvent.ACTION_DOWN:
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            x = motionEvent.getX();
                            return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            };
            wholeView.setOnTouchListener(listener);
            accountNameView = (IMECloseListenableEditText) itemView.findViewById(R.id.account_name_view);
            accountNameView.setOnEditTextCloseListener(new IMECloseListenableEditText.OnEditTextCloseListener() {

                @Override
                public void onEditTextClose(String text) {
                    if (!TextUtils.isEmpty(text)) {
                        int position;
                        AccountDataModel accountDataModel = items.get(position = getPosition());
                        for (AccountDataModel acc : items) {
                            if (acc.getAccountName().contentEquals(text)) {
                                accountDataModel.setAccountName(accountDataModel.getAccountName());
                                return;
                            }
                        }
                        accountDataModel.setAccountName(text);
                        AccountManager.getInstance().setAccountName(accountDataModel.getAccountName(), text);
                        notifyItemChanged(position);
                    }
                    mActivity.findViewById(android.R.id.content).requestFocus();
                }
            });
        }
    }

    public static class AccountDataModel {
        private final int id;

        private String accountName;
        private boolean selected;

        public AccountDataModel(int _id, String _accountName, Boolean _selected) {
            id = _id;
            accountName = _accountName;
            selected = _selected;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public String getAccountName() {
            return accountName;
        }

        public int getAccountId() {
            return id;
        }

        @Override
        public int hashCode() {
            return getAccountId();
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof AccountDataModel && o.hashCode() == hashCode());
        }

        public boolean isSelected() {
            return selected;
        }

        private void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
}
