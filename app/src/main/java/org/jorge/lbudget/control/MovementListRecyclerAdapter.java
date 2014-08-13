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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.jorge.lbudget.R;
import org.jorge.lbudget.ui.utils.undobar.UndoBar;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class MovementListRecyclerAdapter extends RecyclerView.Adapter<MovementListRecyclerAdapter.ViewHolder> {

    private final float MIN_SWIPE_WIDTH_PIXELS;
    private final Activity mActivity;
    private final RecyclerView mRecyclerView;
    private List<MovementDataModel> items;
    @SuppressWarnings("FieldCanBeLocal")
    private final int itemLayout = R.layout.list_item_movement_list;
    private static int incomeColor, expenseColor;
    private Context mContext;

    public MovementListRecyclerAdapter(RecyclerView recyclerView, Activity activity, Context context, List<MovementDataModel> items) {
        this.items = items;
        mContext = context;
        mActivity = activity;
        MIN_SWIPE_WIDTH_PIXELS = LBudgetUtils.getInt(context, "min_swipe_width_pixels");
        mRecyclerView = recyclerView;
    }

    public static void updateMovementColors(Context context) {
        updateIncomeColor(context);
        updateExpenseColor(context);
    }

    private static void updateIncomeColor(Context context, String... newColor) {
        incomeColor = getMovementColorFromPreferences(context, "pref_key_movement_income_color", newColor.length <= 0 ? LBudgetUtils.getString(context, "movement_color_green_identifier") : newColor[0]);
    }

    private static void updateExpenseColor(Context context, String... newColor) {
        expenseColor = getMovementColorFromPreferences(context, "pref_key_movement_expense_color", newColor.length <= 0 ? LBudgetUtils.getString(context, "movement_color_red_identifier") : newColor[0]);
    }

    private static int getMovementColorFromPreferences(Context context, String prefName, String defaultColor) {
        int retId = -1;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String prefKey = LBudgetUtils.getString(context, prefName);
        final String identifier = sharedPreferences.getString(prefKey, LBudgetUtils.getString(context, "movement_color_" + defaultColor.toLowerCase() + "_identifier"));
        if (identifier.contentEquals(LBudgetUtils.getString(context, "movement_color_red_identifier"))) {
            retId = R.color.movement_color_red;
        } else if (identifier.contentEquals(LBudgetUtils.getString(context, "movement_color_green_identifier"))) {
            retId = R.color.movement_color_green;
        } else if (identifier.contentEquals(LBudgetUtils.getString(context, "movement_color_blue_identifier"))) {
            retId = R.color.movement_color_blue;
        }
        return context.getResources().getColor(retId);
    }

    public void add(MovementDataModel item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
        //TODO Add movement to db
    }

    public MovementDataModel remove(int position) {
        MovementDataModel ret = items.remove(position);
        notifyItemRemoved(position);
        //TODO Remove movement from db
        return ret;
    }

    private void sendShareIntent(MovementDataModel item) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final String textMime = "text/plain", fullMimes = textMime + "image/*";
        final boolean hasPicture, isIncome = item.getAmount() >= 0;
        intent.setType((hasPicture = movementHasPicture(item)) ? fullMimes : textMime);
        intent.putExtra(Intent.EXTRA_TITLE, item.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, (isIncome ? mContext.getString(R.string.share_text_income) : mContext.getString(R.string.share_text_expense)).replace("{MONEYPLACEHOLDER}", MovementDataModel.printifyMoneyAmount(mContext, item.getAmount())) + AccountManager.getInstance().getSelectedAccount().getAccountCurrency());
        if (hasPicture) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(item.getImagePath())));
        }

        mActivity.startActivity(Intent.createChooser(intent, LBudgetUtils.getString(mContext, "share_dialog_title")));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, viewGroup, Boolean.FALSE);
        v.findViewById(R.id.movement_button_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendShareIntent(items.get(i));
            }
        });
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        MovementDataModel item = items.get(i);
        viewHolder.movementNameView.setText(item.getTitle());
        long amount = item.getAmount();
        viewHolder.movementTypeView.setBackgroundColor(amount >= 0 ? incomeColor : expenseColor);
        viewHolder.movementAmountView.setText(MovementDataModel.printifyMoneyAmount(mContext, amount) + " " + AccountManager.getInstance().getSelectedAccount().getAccountCurrency());
        if (movementHasPicture(item)) {
            viewHolder.movementImageView.setImageDrawable(Drawable.createFromPath(item.getImagePath()));
            viewHolder.movementImageView.setVisibility(View.VISIBLE);
        }
    }

    private boolean movementHasPicture(MovementDataModel item) {
        final String fileSeparator = LBudgetUtils.getString(mContext, "symbol_file_separator");
        File target = new File(mContext.getExternalFilesDir(LBudgetUtils.getString(mContext, "picture_folder_name")) + fileSeparator + item.getId() + LBudgetUtils.getString(mContext, "camera_image_extension"));
        return target.exists();
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
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Required for onTouchListener to work.
                }
            });
            itemView.setOnTouchListener(new View.OnTouchListener() {
                private float x = 0;

                @Override
                public boolean onTouch(final View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            final float diff = motionEvent.getX() - x;
                            if (Math.abs(diff) >= MIN_SWIPE_WIDTH_PIXELS) {
                                final Animation fadeOut = AnimationUtils.loadAnimation(mContext, diff < 0 ? R.anim.fade_out_to_left : R.anim.fade_out_to_right);
                                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        view.setVisibility(View.GONE);
                                        final MovementDataModel movement = remove(getPosition());
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
                            } else {
                                //TODO onClick (Movement)
                            }
                            break;
                        case MotionEvent.ACTION_DOWN:
                            x = motionEvent.getX();
                            break;
                    }
                    return false;
                }
            });
            movementNameView = (TextView) itemView.findViewById(R.id.movement_name_view);
            movementAmountView = (TextView) itemView.findViewById(R.id.movement_amount_view);
            movementImageView = (ImageView) itemView.findViewById(R.id.movement_image_view);
            movementTypeView = itemView.findViewById(R.id.movement_type_view);
        }
    }

    public static class MovementDataModel {
        private final int id; //The id will be used to find the image
        private final String title;
        private final long amount;

        public static String printifyMoneyAmount(Context context, long amount) {
            final int decimalPlaces = LBudgetUtils.getInt(context, "amount_of_decimals_allowed");
            double val = Math.abs(amount) / (Math.pow(10, decimalPlaces));
            BigDecimal bigDecimal = new BigDecimal(val);
            bigDecimal = bigDecimal.setScale(decimalPlaces, BigDecimal.ROUND_HALF_DOWN);
            return bigDecimal.toPlainString();
        }

        public long getAmount() {
            return amount;
        }

        public String getTitle() {
            return title;
        }

        public int getId() {
            return id;
        }

        public MovementDataModel(int id, String info, long amount) {
            this.id = id;
            this.title = info;
            this.amount = amount;
        }

        public String getImagePath() {
            //TODO getImagePath
            return null;
        }
    }
}
