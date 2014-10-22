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

import org.jorge.lbudget.R;
import org.jorge.lbudget.ui.navbar.NavigationToolbarButton;
import org.jorge.lbudget.ui.navbar.NavigationToolbarRecyclerAdapter;

public class NavigationToolbarFragment extends Fragment implements NavigationToolbarRecyclerAdapter.NavigationToolbarRecyclerAdapterOnItemClickListener {

    private NavigationToolbarListener mCallback;
    private ImageView mWedgeView;

    public NavigationToolbarButton getNavigationToolbarButton() {
        return mNavigationToolbarButton;
    }

    private NavigationToolbarButton mNavigationToolbarButton;
    private Context mContext;

    public void setSelectedIndex(int i) {
        mNavigationToolbarButton.setSelectedIndex(i);
    }

    public static interface NavigationToolbarListener {
        public void onMenuSelected(int index);
    }

    public void onNavigationItemSelected(int index) {
        mCallback.onMenuSelected(index);
        mNavigationToolbarButton.setSelectedIndex(index);
        mNavigationToolbarButton.initCloseProtocol();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_navigation_toolbar, container, Boolean.FALSE);
        mNavigationToolbarButton = (NavigationToolbarButton) ret.findViewById(R.id.navigation_toolbar_selected);
        mWedgeView = (ImageView) ret.findViewById(R.id.navigation_toolbar_wedge);
        return ret;
    }

    public void rotateWedge(Boolean clockwise) {
        Animation animationRotate = AnimationUtils.loadAnimation(mContext, R.anim.rotate_clockwise_180);
        if (!clockwise) animationRotate.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float v) {
                return Math.abs(v - 1f); //Reverse the animation
            }
        });
        animationRotate.setFillAfter(Boolean.TRUE);
        mWedgeView.startAnimation(animationRotate);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mContext = getActivity().getApplicationContext();

        try {
            mCallback = (NavigationToolbarListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + NavigationToolbarRecyclerAdapter.NavigationToolbarRecyclerAdapterOnItemClickListener.class.getName());
        }
    }
}
