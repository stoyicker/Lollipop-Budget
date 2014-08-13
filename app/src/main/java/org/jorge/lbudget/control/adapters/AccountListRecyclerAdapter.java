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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import org.jorge.lbudget.R;
import org.jorge.lbudget.control.AccountManager;
import org.jorge.lbudget.io.net.LBackupAgent;
import org.jorge.lbudget.ui.utils.undobar.UndoBar;
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
    private static float x = 0;

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
        viewHolder.accountNameButton.setText(item.getAccountName());
        viewHolder.accountCurrencyButton.setText(item.getAccountCurrency());
        if (item.isSelected()) {
            viewHolder.wholeView.setBackgroundResource(R.color.selected_card_background);
            viewHolder.accountNameButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_edit_selected, 0, 0, 0);
            viewHolder.accountNameButton.setTextAppearance(mContext, R.style.AccountNameTextSelected);
            viewHolder.accountCurrencyButton.setTextAppearance(mContext, R.style.AccountCurrencyTextSelected);
        } else {
            viewHolder.wholeView.setBackgroundResource(R.color.non_selected_card_background);
            viewHolder.accountNameButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_edit_non_selected, 0, 0, 0);
            viewHolder.accountNameButton.setTextAppearance(mContext, R.style.AccountNameTextNonSelected);
            viewHolder.accountCurrencyButton.setTextAppearance(mContext, R.style.AccountCurrencyTextNonSelected);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final Button accountNameButton, accountCurrencyButton;
        private final View wholeView;

        public ViewHolder(View itemView) {
            super(itemView);
            wholeView = itemView;
            View.OnTouchListener listener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Log.d("debug", "onTouch");
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
                                            final AccountDataModel movement = remove(getPosition());
                                            new UndoBar.Builder(mActivity)
                                                    .setMessage(LBudgetUtils.getString(mContext, "movement_list_item_removal"))
                                                    .setListener(new UndoBar.Listener() {
                                                        @Override
                                                        public void onHide() {
                                                        }

                                                        @Override
                                                        public void onUndo(Parcelable token) {
                                                            int pos;
                                                            add(movement, pos = getPosition());
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
                                x = 0;
                                if (view.equals(wholeView)) {
                                    setSelectedAccount(getPosition());
                                    return Boolean.TRUE;
                                }
                            }
                        case MotionEvent.ACTION_DOWN:
                            x = motionEvent.getX();
                            return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            };
            wholeView.setOnTouchListener(listener);
            accountNameButton = (Button) itemView.findViewById(R.id.account_name_view);
            accountNameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO onClick accountNameButton
                    Log.d("debug", "onClick accountNameButton");
                }
            });
            accountNameButton.setOnTouchListener(listener);
            accountCurrencyButton = (Button) itemView.findViewById(R.id.account_currency_view);
            accountCurrencyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO onClick accountCurrencyButton
                    Log.d("debug", "onClick accountCurrencyButton");
                }
            });
            accountCurrencyButton.setOnTouchListener(listener);
        }
    }

    public static class AccountDataModel {
        private final String id, accountName, accountCurrency;
        private boolean selected;

        public AccountDataModel(String _id, String _accountName, String _accountCurrency, Boolean _selected) {
            id = _id;
            accountName = _accountName;
            accountCurrency = _accountCurrency;
            selected = _selected;
        }

        public String getAccountCurrency() {
            return accountCurrency;
        }

        public String getAccountName() {
            return accountName;
        }

        public String getAccountId() {
            return id;
        }

        @Override
        public int hashCode() {
            return Integer.valueOf(getAccountId());
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
