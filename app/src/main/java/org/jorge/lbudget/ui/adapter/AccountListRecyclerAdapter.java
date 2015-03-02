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

package org.jorge.lbudget.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.jorge.lbudget.R;
import org.jorge.lbudget.controller.AccountManager;
import org.jorge.lbudget.controller.MovementManager;
import org.jorge.lbudget.ui.component.undobar.UndoBar;
import org.jorge.lbudget.ui.component.undobar.UndoBarShowStateListener;
import org.jorge.lbudget.util.IMECloseListenableEditText;
import org.jorge.lbudget.util.LBudgetUtils;

import java.util.List;

import static org.jorge.lbudget.ui.adapter.MovementListRecyclerAdapter.MovementDataModel
        .printifyAmount;

public class AccountListRecyclerAdapter extends RecyclerView.Adapter<AccountListRecyclerAdapter
        .ViewHolder> {

    private final Activity mActivity;
    private final Context mContext;
    private final RecyclerView mRecyclerView;
    private final List<AccountDataModel> items;
    @SuppressWarnings("FieldCanBeLocal")
    private final int itemLayout = R.layout.list_item_account;
    private final UndoBarShowStateListener undoBarShowStateListener;

    public AccountListRecyclerAdapter(UndoBarShowStateListener _undoBarShowStateListener,
                                      Activity activity, List<AccountDataModel> accounts,
                                      RecyclerView _recyclerView) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        items = accounts;
        mRecyclerView = _recyclerView;
        undoBarShowStateListener = _undoBarShowStateListener;
    }

    private void setSelectedAccount(int position) {
        AccountDataModel oldSelectedAccount = AccountManager.getInstance().getSelectedAccount(),
                newSelectedAccount = items.get(position);
        oldSelectedAccount.setSelected(Boolean.FALSE);
        newSelectedAccount.setSelected(Boolean.TRUE);
        int oldSelected = items.indexOf(oldSelectedAccount);
        AccountManager.getInstance().setSelectedAccount(newSelectedAccount);
        notifyItemChanged(position);
        notifyItemChanged(oldSelected);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout,
                viewGroup, Boolean.FALSE));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        AccountDataModel item = items.get(i);
        viewHolder.accountNameView.setText(item.getAccountName());
        if (item.isSelected()) {
            viewHolder.wholeView.setBackgroundResource(R.color.selected_card_background);
            viewHolder.accountNameView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable
                    .ic_edit_selected, 0, 0, 0);
            viewHolder.accountNameView.setTextAppearance(mContext, R.style.AccountTextSelected);
            viewHolder.accountNameView.setFocusable(Boolean.FALSE);
            viewHolder.accountNameView.setFocusableInTouchMode(Boolean.FALSE);
            viewHolder.balanceView.setTextAppearance(mContext, R.style.AccountTextSelected);
        } else {
            viewHolder.wholeView.setBackgroundResource(R.color.non_selected_card_background);
            viewHolder.accountNameView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable
                    .ic_edit_non_selected, 0, 0, 0);
            viewHolder.accountNameView.setTextAppearance(mContext, R.style.AccountTextNonSelected);
            viewHolder.accountNameView.setFocusable(Boolean.TRUE);
            viewHolder.accountNameView.setFocusableInTouchMode(Boolean.TRUE);
            viewHolder.balanceView.setTextAppearance(mContext, R.style.AccountTextSelected);
        }
        viewHolder.balanceView.setText(printifyAmount(mContext, item.calculateBalance()) + " " +
                AccountManager.getInstance().getSelectedCurrency(mContext));
    }

    private void removeAccountFromPersistence(AccountDataModel account) {
        AccountManager.getInstance().removeAccount(account);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void createNewAccount() {
        int newId = LBudgetUtils
                .calculateAvailableAccountId();
        final AccountDataModel newAcc = new AccountDataModel(newId,
                LBudgetUtils.getString(mContext, "new_account_default_name") + newId,
                Boolean.FALSE);
        if (AccountManager.getInstance().addAccount(newAcc)) {
            notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(mRecyclerView.getBottom());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final IMECloseListenableEditText accountNameView;
        private final View wholeView;
        private final TextView balanceView;

        public ViewHolder(View itemView) {
            super(itemView);
            wholeView = itemView;
            accountNameView = (IMECloseListenableEditText) itemView.findViewById(R.id
                    .account_name_view);
            accountNameView.setOnEditTextCloseListener(new IMECloseListenableEditText
                    .OnEditTextCloseListener() {

                @Override
                public void onEditTextClose(String text) {
                    if (!TextUtils.isEmpty(text)) {
                        int position;
                        AccountDataModel accountDataModel = items.get(position = getPosition());
                        accountDataModel.setAccountName(text);
                        AccountManager.getInstance().setAccountName(accountDataModel.getAccountId
                                (), text);
                        notifyItemChanged(position);
                    }
                    mActivity.findViewById(android.R.id.content).requestFocus();
                }
            });
            balanceView = (TextView) itemView.findViewById(R.id.balance_view);
        }
    }

    public void runDestroy(final RecyclerView origin, final Integer pos) {
        final View thisView = origin.getChildAt(pos);
        thisView.setVisibility(View.GONE);
        final AccountDataModel account = items.get(pos);
        items.remove(account);
        notifyItemRemoved(pos);
        undoBarShowStateListener.onShowUndoBar();
        new UndoBar.Builder(mActivity)
                .setMessage(LBudgetUtils.getString(mContext,
                        "movement_list_item_removal"))
                .setListener(new UndoBar.Listener() {
                    @Override
                    public void onHide() {
                        thisView.setVisibility(View.VISIBLE);
                        removeAccountFromPersistence(account);
                        undoBarShowStateListener
                                .onHideUndoBar();
                    }

                    @Override
                    public void onUndo(Parcelable token) {
                        items.add(pos, account);
                        thisView.setVisibility(View.VISIBLE);
                        notifyItemInserted(pos);
                        mRecyclerView.smoothScrollToPosition
                                (pos);
                        undoBarShowStateListener
                                .onHideUndoBar();
                    }
                })
                .show();
    }

    public void performClick(View view, Integer position) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        mActivity.findViewById(android.R.id.content).requestFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        setSelectedAccount(position);
    }

    public Boolean isSelected(Integer i) {
        return items.get(i).isSelected();
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

        public long calculateBalance() {
            long ret = 0;
            List<MovementListRecyclerAdapter.MovementDataModel> movementsToDate = MovementManager
                    .getInstance().getAccountMovementsToDate(this);
            for (MovementListRecyclerAdapter.MovementDataModel movementDataModel : movementsToDate)
                ret += movementDataModel.getMovementAmount();
            return ret;
        }

        public String toCsvString() {
            return getAccountId() + "," + getAccountName() + "," + isSelected();
        }
    }
}
