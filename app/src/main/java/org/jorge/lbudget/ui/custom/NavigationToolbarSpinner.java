/*
 * This file is part of LBudget.
 * LBudget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * LBudget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with LBudget. If not, see <http://www.gnu.org/licenses/>.
 */

package org.jorge.lbudget.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class NavigationToolbarSpinner extends Spinner {
    private Boolean open = Boolean.FALSE;
    private OpenStateChangeListener listener;

    private NavigationToolbarSpinner(Context context) {
        super(context);
    }

    public NavigationToolbarSpinner(Context context, int mode) {
        super(context, mode);
    }

    public NavigationToolbarSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationToolbarSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NavigationToolbarSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public NavigationToolbarSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
    }

    @Override
    public boolean performClick() {
        if (listener != null)
            if (open) {
                listener.onSpinnerClosed();
            } else {
                listener.onSpinnerOpened();
            }
        open = !open;
        return super.performClick();
    }

    public void setOnOpenStateChangeLister(OpenStateChangeListener listener) {
        this.listener = listener;
    }

    public interface OpenStateChangeListener {
        void onSpinnerOpened();

        void onSpinnerClosed();
    }
}


