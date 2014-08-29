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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jorge.lbudget.R;
import org.jorge.lbudget.utils.LBudgetUtils;

public class NewMovementDialogFragment extends DialogFragment {

    private Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = activity.getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.dialog_fragment_new_movement, container);

        final Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(Boolean.FALSE);
            dialog.getWindow().getAttributes().windowAnimations = R.style.AnimatedMovementPanelAnimationStyle;
            final String dialogTitle = LBudgetUtils.getString(mContext, "register_movement_dialog_title");
            if (TextUtils.isEmpty(dialogTitle))
                setStyle(STYLE_NO_TITLE, 0);
            else
                dialog.setTitle(dialogTitle);
        }

        return ret;
    }
}
