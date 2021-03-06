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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import org.jorge.lbudget.R;
import org.jorge.lbudget.io.files.FileManager;
import org.jorge.lbudget.ui.adapter.MovementListRecyclerAdapter;
import org.jorge.lbudget.util.LBudgetUtils;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MovementImageDialogFragment extends DialogFragment {

    private static final String KEY_MOVEMENT_TITLE = "MOVEMENT_TITLE",
            KEY_MOVEMENT_IMAGE_PATH = "MOVEMENT_IMAGE_PATH";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private Context mContext;

    public static MovementImageDialogFragment newInstance(Context _context,
                                                          MovementListRecyclerAdapter
                                                                  .MovementDataModel movement) {
        MovementImageDialogFragment ret = new MovementImageDialogFragment();

        Bundle args = new Bundle();
        args.putString(KEY_MOVEMENT_TITLE, movement.getMovementTitle());
        args.putString(KEY_MOVEMENT_IMAGE_PATH, movement.getImagePath(_context));
        ret.setArguments(args);

        return ret;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater().inflate(R
                .layout
                .dialog_fragment_movement_image, null, Boolean.FALSE);

        ImageView imageView = (ImageView) view.findViewById(R.id.movement_image_showcase_view);
        try {
            imageView.setImageDrawable(Drawable.createFromPath(getArguments().getString
                    (KEY_MOVEMENT_IMAGE_PATH)));
            new PhotoViewAttacher(imageView);
        } catch (OutOfMemoryError ignored) { //Too much of an image for you to handle
            imageView.setVisibility(View.GONE);
            view.findViewById(R.id.image_error_alternative).setVisibility(View.VISIBLE);
        }

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            view.findViewById(R.id.button_movement_image_snap).setVisibility(View.GONE);
        }

        view.findViewById(R.id.button_movement_image_snap).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                takeNewMovementPicture();
            }
        });

        Dialog ret = new AlertDialog.Builder(getActivity())
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                MovementImageDialogFragment.this.dismiss();
                            }
                        }
                ).setView(view)
                .create();

        ret.getWindow().getAttributes().windowAnimations = R.style
                .AnimatedMovementPanelAnimationStyle;

        final String movementTitle = getArguments().getString(KEY_MOVEMENT_TITLE);
        if (!TextUtils.isEmpty(movementTitle)) {
            ret.setTitle(movementTitle);
        } else {
            setStyle(STYLE_NO_TITLE, 0);
        }

        return ret;
    }

    private void takeNewMovementPicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            final String path = getArguments().getString(KEY_MOVEMENT_IMAGE_PATH);
            File pathAsFile = new File(path), oldPathAsFile = new File(path + LBudgetUtils
                    .getString(mContext, "old_image_name_appendix"));
            if (pathAsFile.exists() && !pathAsFile.renameTo(oldPathAsFile)) {
                dismiss();
                return;
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(pathAsFile));
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final String path = getArguments().getString(KEY_MOVEMENT_IMAGE_PATH);
        final File oldPathAsFile = new File(path + LBudgetUtils.getString(mContext,
                "old_image_name_appendix"));
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            FileManager.recursiveDelete(oldPathAsFile);
            dismiss();
        } else {
            if (oldPathAsFile.exists() && !oldPathAsFile.renameTo(new File(path)))
                throw new IllegalStateException("Couldn't rename the original image back to the " +
                        "original name");
        }
    }
}