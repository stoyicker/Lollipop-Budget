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

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import org.jorge.lbudget.R;
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
    private int selectedIndex = 0;

    public AccountListRecyclerAdapter(Activity activity, Context context, List<AccountDataModel> accounts, RecyclerView _recyclerView) {
        mActivity = activity;
        mContext = context;
        items = accounts;
        mRecyclerView = _recyclerView;
        MIN_SWIPE_WIDTH_PIXELS = LBudgetUtils.getInt(context, "min_swipe_width_pixels");
    }

    public void add(AccountDataModel item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
        //TODO Add account to file (IMPORTANT TO DO IT HERE, BEFORE THE BACKUP REQUEST)
        LBackupAgent.requestBackup(mContext);
    }

    public AccountDataModel remove(int position) {
        AccountDataModel ret = items.remove(position);
        notifyItemRemoved(position);
        //TODO Remove account from file (IMPORTANT TO DO IT HERE, BEFORE THE BACKUP REQUEST)
        LBackupAgent.requestBackup(mContext);
        return ret;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, viewGroup, Boolean.FALSE));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        AccountDataModel item = items.get(i);
        viewHolder.accountNameView.setText(item.getAccountName());
        viewHolder.accountCurrencyView.setText(item.getAccountCurrency());
        viewHolder.wholeView.setBackgroundResource(getSelectedIndex() == i ? R.color.selected_card_background : R.color.card_background);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final Button accountNameView, accountCurrencyView;
        private final View wholeView;

        public ViewHolder(View itemView) {
            super(itemView);
            wholeView = itemView;
            itemView.setOnTouchListener(new View.OnTouchListener() {
                private float x;

                @Override
                public boolean onTouch(final View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            final float diff = motionEvent.getX() - x;
                            if (Math.abs(diff) >= MIN_SWIPE_WIDTH_PIXELS) {
                                if (getSelectedIndex() != getPosition()) {
                                    final Animation fadeOut = AnimationUtils.loadAnimation(mContext, diff < 0 ? R.anim.fade_out_left : R.anim.fade_out_right);
                                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {
                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            view.setVisibility(View.GONE);
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
                                    view.startAnimation(fadeOut);
                                }
                            } else {
                                setSelectedAccount(getPosition());
                            }
                            break;
                        case MotionEvent.ACTION_DOWN:
                            x = motionEvent.getX();
                            break;
                    }
                    return false;
                }
            });
            accountNameView = (Button) itemView.findViewById(R.id.account_name_view);
            accountCurrencyView = (Button) itemView.findViewById(R.id.account_currency_view);
        }
    }

    private void setSelectedAccount(int position) {
        if (AccountManager.getInstance(mContext).setSelectedAccount(items.get(selectedIndex))) {
            selectedIndex = position;
            LBackupAgent.requestBackup(mContext);
        }
    }

    public static class AccountDataModel {
        private final String id, accountName, accountCurrency;

        public AccountDataModel(String _id, String _accountName, String _accountCurrency) {
            id = _id;
            accountName = _accountName;
            accountCurrency = _accountCurrency;
        }

        public String getAccountCurrency() {
            return accountCurrency;
        }

        public String getAccountName() {
            return accountName;
        }

        public String getId() {
            return id;
        }
    }
}
