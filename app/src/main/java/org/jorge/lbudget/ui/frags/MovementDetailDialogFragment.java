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
import android.text.TextUtils;
import android.view.View;

import org.jorge.lbudget.R;
import org.jorge.lbudget.utils.LBudgetUtils;

public class MovementDetailDialogFragment extends DialogFragment {

    private Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_movement_detail, null);

        final String dialogTitle = LBudgetUtils.getString(mContext, "register_movement_dialog_title");
        if (TextUtils.isEmpty(dialogTitle))
            setStyle(STYLE_NO_TITLE, 0);

        Dialog ret = new AlertDialog.Builder(getActivity()).setView(view).setTitle(dialogTitle).setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
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
