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

package org.jorge.lbudget.logic.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.jorge.lbudget.R;
import org.jorge.lbudget.io.files.FileManager;
import org.jorge.lbudget.logic.controllers.AccountManager;
import org.jorge.lbudget.logic.controllers.MovementManager;
import org.jorge.lbudget.ui.utils.undobar.UndoBar;
import org.jorge.lbudget.ui.utils.undobar.UndoBarShowStateListener;
import org.jorge.lbudget.utils.LBudgetTimeUtils;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static org.jorge.lbudget.logic.adapters.MovementListRecyclerAdapter.MovementDataModel.printifyAmount;

public class MovementListRecyclerAdapter extends RecyclerView.Adapter<MovementListRecyclerAdapter.ViewHolder> {

    private final float MIN_SWIPE_WIDTH_PIXELS;
    private final Activity mActivity;
    private final RecyclerView mRecyclerView;
    private List<MovementDataModel> items;
    @SuppressWarnings("FieldCanBeLocal")
    private final int itemLayout = R.layout.list_item_movement;
    private static int incomeColor, expenseColor;
    private final Context mContext;
    private static float x = Float.MAX_VALUE;
    private final UndoBarShowStateListener undoBarShowStateListener;
    private final MovementImageClickListener movementImageClickListener;
    private View mEmptyView;
    private final MovementEditRequestListener mMovementEditRequestListener;
    private static MovementListRecyclerAdapter publicAccessInstance;

    public MovementListRecyclerAdapter(View emptyView, UndoBarShowStateListener _undoBarShowStateListener, RecyclerView recyclerView, Activity activity, List<MovementDataModel> items, MovementImageClickListener movementImageClickListener, MovementEditRequestListener movementEditRequestListener) {
        this.items = items;
        this.movementImageClickListener = movementImageClickListener;
        mContext = activity.getApplicationContext();
        mActivity = activity;
        MIN_SWIPE_WIDTH_PIXELS = LBudgetUtils.getInt(mContext, "min_swipe_width_pixels");
        mRecyclerView = recyclerView;
        undoBarShowStateListener = _undoBarShowStateListener;
        mEmptyView = emptyView;
        mMovementEditRequestListener = movementEditRequestListener;
        publicAccessInstance = this;
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

    public static int getMovementColorFromPreferences(Context context, String prefName, String defaultColor) {
        int retId = -1;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String prefKey = LBudgetUtils.getString(context, prefName);
        final String identifier = sharedPreferences.getString(prefKey, LBudgetUtils.getString(context, "movement_color_" + defaultColor.toLowerCase(Locale.ENGLISH) + "_identifier"));
        if (identifier.contentEquals(LBudgetUtils.getString(context, "movement_color_red_identifier"))) {
            retId = R.color.movement_color_red;
        } else if (identifier.contentEquals(LBudgetUtils.getString(context, "movement_color_green_identifier"))) {
            retId = R.color.movement_color_green;
        } else if (identifier.contentEquals(LBudgetUtils.getString(context, "movement_color_blue_identifier"))) {
            retId = R.color.movement_color_blue;
        }
        return context.getResources().getColor(retId);
    }

    public static MovementListRecyclerAdapter getPublicAccessInstance() {
        return publicAccessInstance;
    }

//    public void add(MovementDataModel item, int position) {
//        items.add(position, item);
//        notifyItemInserted(position);
//        MovementManager.getInstance().addMovement(item);
//    }
//
//    public MovementDataModel remove(int position) {
//        MovementDataModel ret = items.remove(position);
//        notifyItemRemoved(position);
//        removeMovementFromPersistence(ret);
//        return ret;
//    }

    private void sendShareIntent(MovementDataModel item) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final String textMime = "text/plain", fullMimes = textMime + "image/*";
        final boolean hasPicture, isIncome = item.getMovementAmount() >= 0;
        intent.setType((hasPicture = new File(item.getImagePath(mContext)).exists()) ? fullMimes : textMime);
        intent.putExtra(Intent.EXTRA_TITLE, item.getMovementTitle());
        intent.putExtra(Intent.EXTRA_TEXT, (isIncome ? mContext.getString(R.string.share_text_income) : mContext.getString(R.string.share_text_expense)).replace(LBudgetUtils.getString(mContext, "amount_placeholder"), printifyAmount(mContext, item.getMovementAmount())) + AccountManager.getInstance().getSelectedCurrency(mContext));
        if (hasPicture) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(item.getImagePath(mContext))));
        }

        mActivity.startActivity(Intent.createChooser(intent, LBudgetUtils.getString(mContext, "share_dialog_title")));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, viewGroup, Boolean.FALSE);
        v.findViewById(R.id.button_share_movement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendShareIntent(items.get(i));
            }
        });
        v.findViewById(R.id.movement_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movementImageClickListener.onMovementImageClick(items.get(i));
            }
        });
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        MovementDataModel item = items.get(i);
        viewHolder.movementNameView.setText(item.getMovementTitle());
        long amount = item.getMovementAmount();
        viewHolder.movementTypeView.setBackgroundColor(amount >= 0 ? incomeColor : expenseColor);
        viewHolder.movementAmountView.setText(printifyAmount(mContext, amount) + " " + AccountManager.getInstance().getSelectedCurrency(mContext));
        viewHolder.movementEpochView.setText(LBudgetTimeUtils.getTimeAgo(item.getMovementEpoch(), mContext));
        final String imagePath = item.getImagePath(mContext);
        if (new File(imagePath).exists()) {
            try {
                viewHolder.movementImageView.setImageDrawable(Drawable.createFromPath(imagePath));
                viewHolder.movementImageView.setVisibility(View.VISIBLE);
            } catch (OutOfMemoryError ignored) {
                //Too much of an image for you to handle
            }
        }
    }

    private void removeMovementFromPersistence(MovementDataModel movement) {
        MovementManager.getInstance().removeMovement(movement);
        FileManager.recursiveDelete(new File(movement.getImagePath(mContext)));
    }

    @Override
    public int getItemCount() {
        mEmptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        return items.size();
    }

    public void refreshItemSet() {
        items = MovementManager.getInstance().getSelectedAccountMovementsToDate();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView movementNameView, movementAmountView, movementEpochView;
        private final ImageView movementImageView;
        private final View movementTypeView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(null); //Required for onTouchListener.
            itemView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(final View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            final float diff = motionEvent.getX() - x;
                            if (x != Float.MAX_VALUE && Math.abs(diff) >= MIN_SWIPE_WIDTH_PIXELS) {
                                final Animation fadeOut = AnimationUtils.loadAnimation(mContext, diff < 0 ? R.anim.fade_out_to_left : R.anim.fade_out_to_right);
                                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        view.setVisibility(View.GONE);
                                        int pos;
                                        final MovementDataModel movement = items.remove(pos = getPosition());
                                        notifyItemRemoved(pos);
                                        undoBarShowStateListener.onShowUndoBar();
                                        new UndoBar.Builder(mActivity)
                                                .setMessage(LBudgetUtils.getString(mContext, "movement_list_item_removal"))
                                                .setListener(new UndoBar.Listener() {
                                                    @Override
                                                    public void onHide() {
                                                        removeMovementFromPersistence(movement);
                                                        undoBarShowStateListener.onHideUndoBar();
                                                    }

                                                    @Override
                                                    public void onUndo(Parcelable token) {
                                                        int pos;
                                                        items.add(pos = getPosition() > -1 ? getPosition() : 0, movement);
                                                        notifyItemInserted(pos);
                                                        mRecyclerView.smoothScrollToPosition(pos);
                                                        undoBarShowStateListener.onHideUndoBar();
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
                                x = Float.MAX_VALUE; //Reset x
                                mMovementEditRequestListener.onMovementEditRequested(items.get(getPosition()));
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
            movementEpochView = (TextView) itemView.findViewById(R.id.movement_epoch_view);
        }
    }

    public static class MovementDataModel {
        private final int id; //The id will be used to find the image
        private String title;
        private Long amount, epoch;

        public Long getMovementAmount() {
            return amount;
        }

        public String getMovementTitle() {
            return TextUtils.isEmpty(title) ? "" : title;
        }

        public int getMovementId() {
            return id;
        }

        public MovementDataModel(int id, String info, long amount, long epoch) {
            this.id = id;
            this.title = info;
            this.amount = amount;
            this.epoch = epoch;
        }

        public String getImagePath(Context _context) {
            final String fileSeparator = LBudgetUtils.getString(_context, "symbol_file_separator");
            File target = new File(_context.getExternalFilesDir(LBudgetUtils.getString(_context, "picture_folder_name")) + fileSeparator + getMovementId() + LBudgetUtils.getString(_context, "camera_image_extension"));
            return target.getAbsolutePath();
        }

        public long getMovementEpoch() {
            return epoch;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public void setEpoch(Long epoch) {
            this.epoch = epoch;
        }

        public static String printifyAmount(Context context, long amount) {
            final int decimalPlaces = LBudgetUtils.getInt(context, "amount_of_decimals_allowed");
            double val = Math.abs(amount) / (Math.pow(10, decimalPlaces));
            BigDecimal bigDecimal = new BigDecimal(val);
            bigDecimal = bigDecimal.setScale(decimalPlaces, BigDecimal.ROUND_HALF_DOWN);
            return bigDecimal.toPlainString();
        }

        public static Long processStringAmount(String s) {
            final String DECIMAL_DOT = ".";
            int decimals;
            Long ret;
            if (s.contains(DECIMAL_DOT)) {
                decimals = 0;
                ret = new BigDecimal(TextUtils.isEmpty(s) ? "" : s.substring(0, decimals = s.indexOf(DECIMAL_DOT))).longValue();
                ret *= 100;
                ret += s.substring(decimals + 1).length() > 2 ? new BigDecimal(s.substring(decimals + 1, decimals + 3)).longValue() : new BigDecimal(s.substring(decimals + 1)).longValue();
            } else ret = new BigDecimal(s).longValue() * 100;
            return ret;
        }

        @Override
        public String toString() {
            return getMovementTitle();
        }

        public String toCsvString() {
            return getMovementId() + "," + getMovementTitle() + "," + getMovementAmount() + "," + getMovementEpoch();
        }
    }

    public interface MovementImageClickListener {
        public void onMovementImageClick(MovementDataModel movement);
    }

    public interface MovementEditRequestListener {
        public void onMovementEditRequested(MovementListRecyclerAdapter.MovementDataModel movement);
    }
}