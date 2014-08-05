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
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Spinner;

import org.jorge.lbudget.R;
import org.jorge.lbudget.ui.custom.NavigationToolbarSpinner;

public class NavigationToolbarFragment extends Fragment {

    private NavigationToolbarListener mCallback;
    private ImageView wedgeView;
    private NavigationToolbarSpinner navigationSpinner;

    public static interface NavigationToolbarListener {
        public void onMenuSelected(int index);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_navigation_toolbar, container, false);
        navigationSpinner = (NavigationToolbarSpinner) ret.findViewById(R.id.navigation_toolbar_selector);
        wedgeView = (ImageView) ret.findViewById(R.id.navigation_toolbar_wedge);
        navigationSpinner.setOnOpenStateChangeLister(new NavigationToolbarSpinner.OpenStateChangeListener() {
            @Override
            public void onSpinnerOpened() {
                openNavigationMenu();
            }

            @Override
            public void onSpinnerClosed() {
                closeNavigationMenu();
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
