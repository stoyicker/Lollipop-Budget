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

package org.jorge.lbudget.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.jorge.lbudget.R;
import org.jorge.lbudget.controller.AccountManager;
import org.jorge.lbudget.controller.MovementManager;
import org.jorge.lbudget.io.files.FileManager;
import org.jorge.lbudget.ui.adapter.MovementListRecyclerAdapter;
import org.jorge.lbudget.util.LBudgetTimeUtils;
import org.jorge.lbudget.util.LBudgetUtils;

import java.io.File;
import java.util.StringTokenizer;

import uk.co.senab.photoview.PhotoViewAttacher;

import static org.jorge.lbudget.ui.adapter.MovementListRecyclerAdapter
        .getMovementColorFromPreferences;

public class MovementDetailDialogFragment extends DialogFragment {

    private static final String KEY_MOVEMENT_ID = "MOVEMENT_ID",
            KEY_MOVEMENT_TITLE = "MOVEMENT_TITLE", KEY_MOVEMENT_AMOUNT = "MOVEMENT_AMOUNT",
            KEY_MOVEMENT_EPOCH = "MOVEMENT_EPOCH", KEY_MOVEMENT_IMAGE_PATH = "MOVEMENT_IMAGE_PATH";
    private Context mContext;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private PhotoViewAttacher mPhotoViewAttacher;
    private ImageView mPhotoView;

    /**
     * To be used when editing a movement.
     *
     * @param movement {@link org.jorge.lbudget.ui.adapter.MovementListRecyclerAdapter
     *                 .MovementDataModel} The movement to edit.
     * @return {@link org.jorge.lbudget.ui.fragment.MovementDetailDialogFragment} The ready-to-use
     * fragment
     */
    public static MovementDetailDialogFragment newInstance(Context context,
                                                           @NonNull MovementListRecyclerAdapter
                                                                   .MovementDataModel movement) {
        MovementDetailDialogFragment ret = new MovementDetailDialogFragment();

        Bundle args = new Bundle();

        args.putInt(KEY_MOVEMENT_ID, movement.getMovementId());
        args.putString(KEY_MOVEMENT_TITLE, movement.getMovementTitle());
        args.putLong(KEY_MOVEMENT_AMOUNT, movement.getMovementAmount());
        args.putLong(KEY_MOVEMENT_EPOCH, movement.getMovementEpoch());
        args.putString(KEY_MOVEMENT_IMAGE_PATH, movement.getImagePath(context));

        ret.setArguments(args);

        return ret;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }

    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater().inflate(R
                .layout
                .dialog_fragment_movement_detail, null, Boolean.FALSE);

        final Bundle args = getArguments();

        final String dialogTitle;
        final DialogInterface.OnClickListener onPositiveButtonClickListener;
        final Button expenseButton = (Button) view.findViewById(R.id
                .movement_detail_type_expense_view), incomeButton = (Button) view.findViewById(R
                .id.movement_detail_type_income_view);
        final EditText dateView = (EditText) view.findViewById(R.id.movement_detail_date_view),
                titleView = (EditText) view.findViewById(R.id.movement_detail_title_view),
                amountView = (EditText) view.findViewById(R.id.movement_detail_amount_view);
        Long epoch = System.currentTimeMillis();
        String imagePath;

        ((TextView) view.findViewById(R.id.movement_detail_currency_view)).setText(AccountManager
                .getInstance().getSelectedCurrency(mContext));
        mPhotoView = (ImageView) view.findViewById(R.id.movement_image_showcase_view);

        if (args == null) {
            imagePath = new MovementListRecyclerAdapter.MovementDataModel(LBudgetUtils
                    .calculateAvailableMovementId(), "stub", -23, 5).getImagePath(mContext);
            //The data around the object is just random stub to be able to get the image
            // path
            dialogTitle = LBudgetUtils.getString(mContext, "register_movement_dialog_title");
            onPositiveButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Long amount;
                            try {
                                amount = (MovementListRecyclerAdapter.MovementDataModel
                                        .processStringAmount(amountView.getText().toString()) *
                                        (incomeButton.getVisibility() == View.VISIBLE ? 1 : -1));
                            } catch (NumberFormatException ex) {
                                dismiss();
                                return;
                            }
                            if (amount == 0L) {
                                dismiss();
                                return;
                            }
                            String epochAs8601 = dateView.getText().toString(),
                                    title = titleView.getText().toString();
                            MovementDetailDialogFragment.this.addMovement(title, amount,
                                    LBudgetTimeUtils.ISO8601AsEpoch(mContext, epochAs8601));
                        }
                    };
        } else {
            imagePath = args.getString(KEY_MOVEMENT_IMAGE_PATH);
            dialogTitle = LBudgetUtils.getString(mContext, "edit_movement_dialog_title");
            onPositiveButtonClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Long amount = (MovementListRecyclerAdapter.MovementDataModel
                            .processStringAmount(amountView.getText().toString()) * (incomeButton
                            .getVisibility() == View.VISIBLE ? 1 : -1));
                    String epochAs8601, title = titleView.getText().toString();
                    if (!(epochAs8601 = dateView.getText().toString()).contentEquals
                            (LBudgetTimeUtils.epochAsISO8601(mContext,
                                    args.getLong(KEY_MOVEMENT_EPOCH))) || !title.contentEquals
                            (args.getString(KEY_MOVEMENT_TITLE)) || amount != args.getLong
                            (KEY_MOVEMENT_AMOUNT)) {
                        MovementDetailDialogFragment.this.updateMovement(args.getInt
                                (KEY_MOVEMENT_ID), title, amount, LBudgetTimeUtils.ISO8601AsEpoch
                                (mContext, epochAs8601));
                    }
                }
            };
            if (args.getLong(KEY_MOVEMENT_AMOUNT) > 0) {
                expenseButton.setVisibility(View.GONE);
                incomeButton.setVisibility(View.VISIBLE);
            }
            mPhotoView.setImageDrawable(Drawable.createFromPath(args.getString
                    (KEY_MOVEMENT_IMAGE_PATH)));
            mPhotoViewAttacher = new PhotoViewAttacher(mPhotoView);
            epoch = args.getLong(KEY_MOVEMENT_EPOCH);
            titleView.setText(args.getString(KEY_MOVEMENT_TITLE));
            amountView.setText(MovementListRecyclerAdapter.MovementDataModel.printifyAmount
                    (mContext, Math.abs(args.getLong(KEY_MOVEMENT_AMOUNT))));
        }

        final String imagePathAsFinal = imagePath;

        view.findViewById(R.id.button_movement_image_snap).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                takeNewMovementPicture(imagePathAsFinal);
            }
        });

        final String epochAsIso8601 = LBudgetTimeUtils.epochAsISO8601(mContext, epoch);
        dateView.setText(epochAsIso8601);
        final StringTokenizer epochAsISO8601Tokenizer = new StringTokenizer(epochAsIso8601, "-");
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        dateView.setText(year + "-" + (month < 9 ? "0" : "") + (month + 1) + "-"
                                + (day < 10 ? "0" : "") + day);
                    }
                }, Integer.parseInt(epochAsISO8601Tokenizer.nextToken()),
                        Integer.parseInt(epochAsISO8601Tokenizer.nextToken()) - 1,
                        Integer.parseInt(epochAsISO8601Tokenizer.nextToken())).show();
            }
        });

        setMovementTypeButtonBackground(incomeButton, getMovementColorFromPreferences(mContext,
                "pref_key_movement_income_color", LBudgetUtils.getString(mContext,
                        "movement_color_green_identifier")));
        setMovementTypeButtonBackground(expenseButton, getMovementColorFromPreferences(mContext,
                "pref_key_movement_expense_color", LBudgetUtils.getString(mContext,
                        "movement_color_red_identifier")));

        expenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expenseButton.setVisibility(View.GONE);
                incomeButton.setVisibility(View.VISIBLE);
            }
        });

        incomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incomeButton.setVisibility(View.GONE);
                expenseButton.setVisibility(View.VISIBLE);
            }
        });

        if (TextUtils.isEmpty(dialogTitle))
            setStyle(STYLE_NO_TITLE, 0);

        Dialog ret = new AlertDialog.Builder(getActivity()).setView(view).setTitle(dialogTitle)
                .setPositiveButton(android.R.string.ok, onPositiveButtonClickListener
                ).setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                FileManager.recursiveDelete(new File(new MovementListRecyclerAdapter
                                        .MovementDataModel(LBudgetUtils
                                        .calculateAvailableMovementId(),
                                        "stub", -23, 5).getImagePath(mContext))); //The data
                                // around the
                                // object is just random stub to be able to get the image path
                                MovementDetailDialogFragment.this.dismiss();
                            }
                        }
                ).create();

        ret.setCanceledOnTouchOutside(args != null);

        ret.getWindow().getAttributes().windowAnimations = R.style
                .AnimatedMovementPanelAnimationStyle;

        return ret;
    }

    private void addMovement(String title, Long amount, long epoch) {
        MovementListRecyclerAdapter.MovementDataModel movement = new MovementListRecyclerAdapter
                .MovementDataModel(LBudgetUtils.calculateAvailableMovementId(), title, amount,
                epoch);
        MovementListRecyclerAdapter.getPublicAccessInstance().add(movement);
    }

    private void updateMovement(int id, String newTitle, Long newAmount, Long newEpoch) {
        if (MovementManager.getInstance().updateMovement(id, newTitle, newAmount, newEpoch))
            MovementListRecyclerAdapter.getPublicAccessInstance().refreshItemSet();
    }

    private void setMovementTypeButtonBackground(Button button, int movementColorFromPreferences) {
        final int MOVEMENT_COLOR_RED = mContext.getResources().getColor(R.color
                .movement_color_red), MOVEMENT_COLOR_GREEN = mContext.getResources().getColor(R
                .color.movement_color_green), MOVEMENT_COLOR_BLUE = mContext.getResources()
                .getColor(R.color.movement_color_blue);

        int background;

        if (movementColorFromPreferences == MOVEMENT_COLOR_RED) {
            background = R.drawable.movement_type_background_ripple_red;
        } else if (movementColorFromPreferences == MOVEMENT_COLOR_GREEN) {
            background = R.drawable.movement_type_background_ripple_green;
        } else if (movementColorFromPreferences == MOVEMENT_COLOR_BLUE) {
            background = R.drawable.movement_type_background_ripple_blue;
        } else
            throw new IllegalStateException("Unrecognized movement color found when rendering the" +
                    " movement type button.");

        button.setBackgroundResource(background);
    }

    private void takeNewMovementPicture(final String imagePath) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File pathAsFile = new File(imagePath), oldPathAsFile = new File(imagePath +
                    LBudgetUtils.getString(mContext, "old_image_name_appendix"));
            if (pathAsFile.exists() && !pathAsFile.renameTo(oldPathAsFile)) {
                return;
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(pathAsFile));
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final String path = new MovementListRecyclerAdapter.MovementDataModel(LBudgetUtils
                .calculateAvailableMovementId(), "stub", -23, 5).getImagePath(mContext); //The
        // data around the object is just random stub to be able to get the image path
        final File oldPathAsFile = new File(path + LBudgetUtils.getString(mContext,
                "old_image_name_appendix"));
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            FileManager.recursiveDelete(oldPathAsFile);
            mPhotoView.setImageDrawable(Drawable.createFromPath(path));
            if (mPhotoViewAttacher == null)
                mPhotoViewAttacher = new PhotoViewAttacher(mPhotoView);
            mPhotoViewAttacher.update();
        } else {
            if (oldPathAsFile.exists() && !oldPathAsFile.renameTo(new File(path)))
                throw new IllegalStateException("Couldn't rename the original image back to the " +
                        "original name");
        }
    }
}
