/*
 * This file is part of LBudget.
 * LBudget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.util.List;

@SuppressWarnings("unused")
public class NavigationToolbarRecyclerAdapter extends RecyclerView.Adapter<NavigationToolbarRecyclerAdapter.ViewHolder> {

    private List<NavigationToolbarDataModel> items;
    @SuppressWarnings("FieldCanBeLocal")
    private final int itemLayout = R.layout.list_item_navigation_toolbar;
    private NavigationToolbarRecyclerAdapterOnItemClickListener mCallback;
    private final NavigationToolbarSelectionRecorder mSelectionRecorder;
    private final Context mContext;

    public interface NavigationToolbarSelectionRecorder {
        public int getSelectedItemPosition();
    }

    public NavigationToolbarRecyclerAdapter(Context context, List<NavigationToolbarDataModel> items, NavigationToolbarRecyclerAdapterOnItemClickListener callback, NavigationToolbarSelectionRecorder _selectionRecorder) {
        this.items = items;
        mCallback = callback;
        mSelectionRecorder = _selectionRecorder;
        mContext = context;
    }

    public void add(NavigationToolbarDataModel item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(NavigationToolbarDataModel item) {
        int position = items.indexOf(item);
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, viewGroup, false);
        return new ViewHolder(mContext, v, mCallback);
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

        public ViewHolder(Context context, View itemView, NavigationToolbarRecyclerAdapterOnItemClickListener callback) {
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
}
