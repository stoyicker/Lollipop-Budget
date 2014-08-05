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

package org.jorge.lbudget.ui.frags;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import org.jorge.lbudget.R;

public class NavigationToolbarFragment extends Fragment {

    private NavigationToolbarListener mCallback;
    private ImageView wedgeView;
    private Boolean isOpen;
    private static final String KEY_OPEN_STATE = "IS_OPEN";

    public static interface NavigationToolbarListener {
        public void onMenuSelected();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isOpen = savedInstanceState == null ? Boolean.FALSE : savedInstanceState.getBoolean(KEY_OPEN_STATE);
        //TODO Inflate the menu if isOpen == Boolean.TRUE
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState == null) outState = new Bundle();
        outState.putBoolean(KEY_OPEN_STATE, isOpen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_navigation_toolbar, container, false);
        wedgeView = (ImageView) ret.findViewById(R.id.menu_selector_wedge);
        ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen) {
                    closeNavigationMenu();
                } else {
                    openNavigationMenu();
                }
                isOpen = !isOpen;
            }
        });
        return ret;
    }

    private void closeNavigationMenu() {
        rotateWedge(Boolean.FALSE);
    }

    private void rotateWedge(Boolean clockwise) {
        Animation animationRotate = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_clockwise_180);
        if (!clockwise) animationRotate.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float v) {
                return Math.abs(v - 1f); //Reverse the animation
            }
        });
        animationRotate.setFillAfter(Boolean.TRUE);
        wedgeView.startAnimation(animationRotate);
    }

    private void openNavigationMenu() {
        rotateWedge(Boolean.TRUE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (NavigationToolbarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NavigationToolbarListener");
        }
    }
}
