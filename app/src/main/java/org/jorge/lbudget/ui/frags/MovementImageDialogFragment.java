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

package org.jorge.lbudget.ui.frags;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jorge.lbudget.R;
import org.jorge.lbudget.io.files.FileManager;
import org.jorge.lbudget.logic.adapters.MovementListRecyclerAdapter;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.io.File;

public class MovementImageDialogFragment extends DialogFragment {

    private static final String KEY_MOVEMENT_TITLE = "MOVEMENT_TITLE", KEY_MOVEMENT_IMAGE_PATH = "MOVEMENT_IMAGE_PATH";
    private static final int REQUEST_TAKE_PHOTO = 1;
    private Context mContext;

    public static MovementImageDialogFragment newInstance(Context _context, MovementListRecyclerAdapter.MovementDataModel movement) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movement_image_dialog, container);

        final Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setTitle(getArguments().getString(KEY_MOVEMENT_TITLE));
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.movement_image_dialog_view);
        try {
            imageView.setImageDrawable(Drawable.createFromPath(getArguments().getString(KEY_MOVEMENT_IMAGE_PATH)));
        } catch (OutOfMemoryError ignored) { //Too much of an image for you to handle
            imageView.setVisibility(View.GONE);
            view.findViewById(R.id.image_error_alternative).setVisibility(View.VISIBLE);
        }

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            view.findViewById(R.id.button_movement_picture_snap).setVisibility(View.GONE);
        }

        view.findViewById(R.id.button_movement_picture_snap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeNewMovementPicture();
            }
        });

        return view;
    }

    private void takeNewMovementPicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            final String path = getArguments().getString(KEY_MOVEMENT_IMAGE_PATH);
            File pathAsFile = new File(path), oldPathAsFile = new File(path + LBudgetUtils.getString(mContext, "old_image_name_appendix"));
            if (!pathAsFile.renameTo(oldPathAsFile)) {
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
        final File oldPathAsFile = new File(path + LBudgetUtils.getString(mContext, "old_image_name_appendix"));
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            FileManager.recursiveDelete(oldPathAsFile);
            dismiss();
        } else {
            if (!oldPathAsFile.renameTo(new File(path)))
                throw new IllegalStateException("Couldn't rename the original image back to the original name");
        }
    }
}