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
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.Spinner;

@SuppressWarnings("unused") //Constructors are necessary for instantiation from XML
public class NavigationToolbarSpinner extends Spinner {
    private boolean mOpenInitiated = Boolean.FALSE;
    private OpenStateChangeListener mListener;

    private NavigationToolbarSpinner(Context context) {
        super(context);
        setMaxDropdownWidth(context);
    }

    public NavigationToolbarSpinner(Context context, int mode) {
        super(context, mode);
        setMaxDropdownWidth(context);
    }

    public NavigationToolbarSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMaxDropdownWidth(context);
    }

    public NavigationToolbarSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMaxDropdownWidth(context);
    }

    public NavigationToolbarSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
        setMaxDropdownWidth(context);
    }

    public NavigationToolbarSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int mode) {
        super(context, attrs, defStyleAttr, defStyleRes, mode);
        setMaxDropdownWidth(context);
    }

    private void setMaxDropdownWidth(Context context) {
        Point target = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(target);
        setDropDownWidth(target.x);
    }

    @Override
    public boolean performClick() {
        mOpenInitiated = true;
        if (mListener != null) {
            mListener.onSpinnerOpened();
        }
        return super.performClick();
    }

    public void setOnOpenStateChangeLister(OpenStateChangeListener listener) {
        this.mListener = listener;
    }

    public void performClosedEvent() {
        mOpenInitiated = false;
        if (mListener != null) {
            mListener.onSpinnerClosed();
        }
    }

    public boolean hasBeenOpened() {
        return mOpenInitiated;
    }

    public interface OpenStateChangeListener {
        void onSpinnerOpened();

        void onSpinnerClosed();
    }
}


