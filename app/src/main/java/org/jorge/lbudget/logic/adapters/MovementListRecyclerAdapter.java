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
import org.jorge.lbudget.utils.LBudgetUtils;
import org.jorge.lbudget.utils.TimeUtils;

import java.io.File;
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
    private static float x = Float.MAX_VALUE;
    private final UndoBarShowStateListener undoBarShowStateListener;
    private View mEmptyView;

    public MovementListRecyclerAdapter(View emptyView, UndoBarShowStateListener _undoBarShowStateListener, RecyclerView recyclerView, Activity activity, List<MovementDataModel> items) {
        this.items = items;
        mContext = activity.getApplicationContext();
        mActivity = activity;
        MIN_SWIPE_WIDTH_PIXELS = LBudgetUtils.getInt(mContext, "min_swipe_width_pixels");
        mRecyclerView = recyclerView;
        undoBarShowStateListener = _undoBarShowStateListener;
        mEmptyView = emptyView;
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
        MovementManager.getInstance().addMovement(item);
    }

    public MovementDataModel remove(int position) {
        MovementDataModel ret = items.remove(position);
        notifyItemRemoved(position);
        removeMovementFromPersistence(ret);
        return ret;
    }

    private void sendShareIntent(MovementDataModel item) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final String textMime = "text/plain", fullMimes = textMime + "image/*";
        final boolean hasPicture, isIncome = item.getMovementAmount() >= 0;
        intent.setType((hasPicture = new File(item.getImagePath(mContext)).exists()) ? fullMimes : textMime);
        intent.putExtra(Intent.EXTRA_TITLE, item.getMovementTitle());
        intent.putExtra(Intent.EXTRA_TEXT, (isIncome ? mContext.getString(R.string.share_text_income) : mContext.getString(R.string.share_text_expense)).replace(LBudgetUtils.getString(mContext, "amount_placeholder"), LBudgetUtils.printifyAmount(mContext, item.getMovementAmount())) + AccountManager.getInstance().getSelectedCurrency(mContext));
        if (hasPicture) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(item.getImagePath(mContext))));
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
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        MovementDataModel item = items.get(i);
        viewHolder.movementNameView.setText(item.getMovementTitle());
        long amount = item.getMovementAmount();
        viewHolder.movementTypeView.setBackgroundColor(amount >= 0 ? incomeColor : expenseColor);
        viewHolder.movementAmountView.setText(LBudgetUtils.printifyAmount(mContext, amount) + " " + AccountManager.getInstance().getSelectedCurrency(mContext));
        viewHolder.movementEpochView.setText(TimeUtils.getTimeAgo(item.getEpoch(), mContext));
        final String imagePath = item.getImagePath(mContext);
        if (new File(imagePath).exists()) {
            viewHolder.movementImageView.setImageDrawable(Drawable.createFromPath(imagePath));
            viewHolder.movementImageView.setVisibility(View.VISIBLE);
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

    public void createNewMovement() {
        //TODO Create new movement
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
                                                        items.add(pos = getPosition(), movement);
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
                                //TODO (CB anywhere) onClick (Movement: Edit it) (Image: View in a modal dialog with the possibility of taking a new one)
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
        private long amount, epoch;

        public long getMovementAmount() {
            return amount;
        }

        public String getMovementTitle() {
            return title;
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

        public long getEpoch() {
            return epoch;
        }
    }
}