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
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jorge.lbudget.R;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.io.File;
import java.util.List;

public class MovementListRecyclerAdapter extends RecyclerView.Adapter<MovementListRecyclerAdapter.ViewHolder> {

    private List<MovementDataModel> items;
    @SuppressWarnings("FieldCanBeLocal")
    private final int itemLayout = R.layout.list_item_movement_list;
    private static int incomeColor, expenseColor;
    private Context mContext;

    public MovementListRecyclerAdapter(Context context, List<MovementDataModel> items) {
        this.items = items;
        mContext = context;
    }

    public void updateMovementColors() {
        incomeColor = getMovementColorFromPreferences("pref_key_movement_income_color", LBudgetUtils.getString(mContext, "movement_color_green_identifier"));
        expenseColor = getMovementColorFromPreferences("pref_key_movement_expense_color", LBudgetUtils.getString(mContext, "movement_color_red_identifier"));
    }

    private int getMovementColorFromPreferences(String prefName, String defaultColor) {
        int retId = -1;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String identifier = sharedPreferences.getString(LBudgetUtils.getString(mContext, prefName), LBudgetUtils.getString(mContext, "movement_color_" + defaultColor.toLowerCase() + "_identifier"));
        Log.d("debug", "No color, identifier is " + identifier);
        if (identifier.contentEquals(LBudgetUtils.getString(mContext, "movement_color_red_identifier"))) {
            Log.d("debug", "Red color");
            retId = R.color.movement_color_red;
        } else if (identifier.contentEquals(LBudgetUtils.getString(mContext, "movement_color_green_identifier"))) {
            Log.d("debug", "Green color");
            retId = R.color.movement_color_green;
        } else if (identifier.contentEquals(LBudgetUtils.getString(mContext, "movement_color_blue_identifier"))) {
            Log.d("debug", "Blue color");
            retId = R.color.movement_color_blue;
        }
        return mContext.getResources().getColor(retId);
    }

    public void add(MovementDataModel item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(MovementDataModel item) {
        int position = items.indexOf(item);
        items.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, viewGroup, Boolean.FALSE);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        MovementDataModel item = items.get(i);
        viewHolder.movementNameView.setText(item.getName());
        long amount = item.getAmount();
        updateMovementColors();
        viewHolder.movementTypeView.setBackgroundColor(amount >= 0 ? incomeColor : expenseColor);
        viewHolder.movementAmountView.setText(LBudgetUtils.printifyMoneyAmount(mContext, amount));
        final String fileSeparator = LBudgetUtils.getString(mContext, "symbol_file_separator");
        File target = new File(mContext.getExternalFilesDir(LBudgetUtils.getString(mContext, "picture_folder_name")) + fileSeparator + item.getId() + LBudgetUtils.getString(mContext, "camera_image_extension"));
        if (target.exists()) {
            viewHolder.movementImageView.setImageDrawable(Drawable.createFromPath(target.getAbsolutePath()));
            viewHolder.movementImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView movementNameView, movementAmountView;
        private final ImageView movementImageView;
        private final View movementTypeView;

        public ViewHolder(View itemView) {
            super(itemView);
            movementNameView = (TextView) itemView.findViewById(R.id.movement_name_view);
            movementAmountView = (TextView) itemView.findViewById(R.id.movement_amount_view);
            movementImageView = (ImageView) itemView.findViewById(R.id.movement_image_view);
            movementTypeView = itemView.findViewById(R.id.movement_type_view);
        }
    }

    public static class MovementDataModel {
        private final int id; //The id will be used to find the image
        private final String name;
        private final long amount;

        public long getAmount() {
            return amount;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public MovementDataModel(int id, String info, long amount) {
            this.id = id;
            this.name = info;
            this.amount = amount;
        }
    }
}
