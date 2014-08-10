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

package org.jorge.lbudget.ui.navbar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jorge.lbudget.R;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.List;

public class NavigationToolbarRecyclerAdapter extends RecyclerView.Adapter<NavigationToolbarRecyclerAdapter.ViewHolder> {

    private List<NavigationToolbarDataModel> items;
    @SuppressWarnings("FieldCanBeLocal")
    private final int itemLayout = R.layout.list_item_navigation_toolbar;
    private NavigationToolbarRecyclerAdapterOnItemClickListener mCallback;
    private final NavigationToolbarSelectionRecorder mSelectionRecorder;

    public interface NavigationToolbarSelectionRecorder {
        public int getSelectedItemPosition();
    }

    public NavigationToolbarRecyclerAdapter(List<NavigationToolbarDataModel> items, NavigationToolbarRecyclerAdapterOnItemClickListener callback, NavigationToolbarSelectionRecorder _selectionRecorder) {
        this.items = items;
        mCallback = callback;
        mSelectionRecorder = _selectionRecorder;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, viewGroup, Boolean.FALSE);
        return new ViewHolder(v, mCallback);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        NavigationToolbarDataModel item = items.get(i);
        String text = item.getText();
        viewHolder.textView.setText(text);
        viewHolder.iconView.setContentDescription(text);
        viewHolder.iconView.setImageResource(item.getIconResId());
        viewHolder.selectedView.setVisibility(i == mSelectionRecorder.getSelectedItemPosition() ? View.VISIBLE : View.GONE);
        viewHolder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView selectedView, iconView;
        private final NavigationToolbarRecyclerAdapterOnItemClickListener mCallback;

        public ViewHolder(View itemView, NavigationToolbarRecyclerAdapterOnItemClickListener callback) {
            super(itemView);
            mCallback = callback;
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int i = getPosition();
                    mCallback.onNavigationItemSelected(i);
                }
            });
            textView = (TextView) itemView.findViewById(R.id.navigation_toolbar_entry_text_view);
            iconView = (ImageView) itemView.findViewById(R.id.navigation_toolbar_entry_icon_view);
            selectedView = (ImageView) itemView.findViewById(R.id.navigation_toolbar_selected_view);
        }

    }

    public interface NavigationToolbarRecyclerAdapterOnItemClickListener {
        public void onNavigationItemSelected(int index);
    }

    public static class NavigationToolbarDataModel {
        private final int iconResId;

        public String getText() {
            return text;
        }

        public int getIconResId() {
            return iconResId;
        }

        private String text;

        public NavigationToolbarDataModel(Context context, int menuId) {
            iconResId = LBudgetUtils.getDrawableAsId("ic_navigation_menu" + menuId);
            text = LBudgetUtils.getStringArray(context, "navigation_items")[menuId];
        }
    }
}
