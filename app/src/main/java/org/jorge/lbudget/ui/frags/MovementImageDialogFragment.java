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

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jorge.lbudget.R;
import org.jorge.lbudget.logic.adapters.MovementListRecyclerAdapter;

public class MovementImageDialogFragment extends DialogFragment {

    private static final String KEY_MOVEMENT_TITLE = "MOVEMENT_TITLE", KEY_MOVEMENT_IMAGE_PATH = "MOVEMENT_IMAGE_PATH";

    public static MovementImageDialogFragment newInstance(Context _context, MovementListRecyclerAdapter.MovementDataModel movement) {
        MovementImageDialogFragment ret = new MovementImageDialogFragment();

        Bundle args = new Bundle();
        args.putString(KEY_MOVEMENT_TITLE, movement.getMovementTitle());
        args.putString(KEY_MOVEMENT_IMAGE_PATH, movement.getImagePath(_context));
        ret.setArguments(args);

        return ret;
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
        return view;
    }
}

//TODO Refresh the movement if the image is new