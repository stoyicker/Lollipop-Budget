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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import org.jorge.lbudget.R;
import org.jorge.lbudget.logic.adapters.MovementListRecyclerAdapter;
import org.jorge.lbudget.utils.LBudgetUtils;

public class MovementDetailDialogFragment extends DialogFragment {

    private static final String KEY_MOVEMENT_TITLE = "MOVEMENT_TITLE", KEY_MOVEMENT_AMOUNT = "MOVEMENT_AMOUNT", KEY_MOVEMENT_EPOCH = "MOVEMENT_EPOCH";
    private Context mContext;

    /**
     * To be used when editing a movement.
     *
     * @param movement {@link org.jorge.lbudget.logic.adapters.MovementListRecyclerAdapter.MovementDataModel} The movement to edit.
     * @return {@link org.jorge.lbudget.ui.frags.MovementDetailDialogFragment} The ready-to-use fragment
     */
    public static MovementDetailDialogFragment newInstance(@NonNull MovementListRecyclerAdapter.MovementDataModel movement) {
        MovementDetailDialogFragment ret = new MovementDetailDialogFragment();

        Bundle args = new Bundle();

        args.putString(KEY_MOVEMENT_TITLE, movement.getMovementTitle());
        args.putLong(KEY_MOVEMENT_AMOUNT, movement.getMovementAmount());
        args.putLong(KEY_MOVEMENT_EPOCH, movement.getMovementEpoch());

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
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_movement_detail, null);

        Bundle args = getArguments();

        final String dialogTitle = LBudgetUtils.getString(mContext, "register_movement_dialog_title");
        if (TextUtils.isEmpty(dialogTitle))
            setStyle(STYLE_NO_TITLE, 0);

        Dialog ret = new AlertDialog.Builder(getActivity()).setView(view).setTitle(dialogTitle).setPositiveButton(android.R.string.ok, args == null ?
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //TODO Save the movement
                                throw new UnsupportedOperationException("Not yet implemented.");
                            }
                        } : new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO Edit the movement
                        throw new UnsupportedOperationException("Not yet implemented.");
                    }
                }
        ).setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        MovementDetailDialogFragment.this.dismiss();
                    }
                }
        ).create();

        ret.setCanceledOnTouchOutside(Boolean.FALSE);

        ret.getWindow().getAttributes().windowAnimations = R.style.AnimatedMovementPanelAnimationStyle;

        return ret;
    }
}
