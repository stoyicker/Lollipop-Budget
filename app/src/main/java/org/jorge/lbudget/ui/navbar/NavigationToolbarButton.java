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

package org.jorge.lbudget.ui.navbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import org.jorge.lbudget.util.LBudgetUtils;

@SuppressWarnings("unused") //Constructors are necessary for instantiation from XML
public class NavigationToolbarButton extends Button implements NavigationToolbarRecyclerAdapter.NavigationToolbarSelectionRecorder {
    private int selectedItemPosition = 0;
    private boolean mOpenInitiated = Boolean.FALSE;
    private OpenStateChangeListener mListener;

    public NavigationToolbarButton(Context context) {
        super(context);
        setText(LBudgetUtils.getStringArray(context, "navigation_items")[selectedItemPosition]);
    }

    public NavigationToolbarButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setText(LBudgetUtils.getStringArray(context, "navigation_items")[selectedItemPosition]);
    }

    public NavigationToolbarButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setText(LBudgetUtils.getStringArray(context, "navigation_items")[selectedItemPosition]);

    }

    public NavigationToolbarButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setText(LBudgetUtils.getStringArray(context, "navigation_items")[selectedItemPosition]);
    }

    @Override
    public boolean performClick() {
        mOpenInitiated = !mOpenInitiated;
        if (mListener != null) {
            if (mOpenInitiated)
                mListener.onOpenRequest();
            else
                mListener.onCloseRequest();
        }
        return Boolean.TRUE;
    }

    public void setOnOpenStateChangeLister(OpenStateChangeListener listener) {
        this.mListener = listener;
    }

    public void initCloseProtocol() {
        mOpenInitiated = Boolean.FALSE;
        if (mListener != null) {
            mListener.onCloseRequest();
            updateTitle();
        }
    }

    public void updateTitle() {
        setText(LBudgetUtils.getStringArray(getContext().getApplicationContext(), "navigation_items")[selectedItemPosition]);
    }

    public boolean hasBeenOpened() {
        return mOpenInitiated;
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void setSelectedIndex(int index) {
        if (selectedItemPosition != index) {
            selectedItemPosition = index;
        }
    }

    public interface OpenStateChangeListener {
        void onOpenRequest();

        void onCloseRequest();
    }
}


