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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import org.jorge.lbudget.ui.navbar.NavigationToolbarDataModel;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.ArrayList;
import java.util.List;

public class NavigationToolbarFragment extends Fragment implements NavigationToolbarRecyclerAdapter.NavigationToolbarRecyclerAdapterOnItemClickListener {

    private NavigationToolbarListener mCallback;
    private ImageView mWedgeView;
    private RecyclerView mNavigationMenuView;

    public NavigationToolbarButton getNavigationToolbarButton() {
        return mNavigationToolbarButton;
    }

    private NavigationToolbarButton mNavigationToolbarButton;
    private Context mContext;

    public static interface NavigationToolbarListener {
        public void onMenuSelected(int index);
    }

    public void onNavigationItemSelected(int index) {
        mNavigationToolbarButton.setSelectedIndex(index);
        mCallback.onMenuSelected(index);
        mNavigationToolbarButton.performClosedEvent();
        RecyclerView.Adapter adapter = mNavigationMenuView.getAdapter();
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        View ret = inflater.inflate(R.layout.fragment_navigation_toolbar, container, false);
        mNavigationToolbarButton = (NavigationToolbarButton) ret.findViewById(R.id.navigation_toolbar_selected);
        mWedgeView = (ImageView) ret.findViewById(R.id.navigation_toolbar_wedge);
        mNavigationMenuView = (RecyclerView) ret.findViewById(R.id.navigation_toolbar_selector);
        mNavigationMenuView.setHasFixedSize(Boolean.TRUE);
        mNavigationMenuView.setLayoutManager(new LinearLayoutManager(mContext));
        mNavigationMenuView.setItemAnimator(new DefaultItemAnimator());
        mNavigationMenuView.setAdapter(new NavigationToolbarRecyclerAdapter(mContext, loadMenuItems(), this, mNavigationToolbarButton));
        mNavigationToolbarButton.setOnOpenStateChangeLister(new NavigationToolbarButton.OpenStateChangeListener() {
            @Override
            public void onOpenRequest() {
                if (mNavigationMenuView.getVisibility() == View.VISIBLE) {
                    //If the menu is already open, close it instead
                    mNavigationToolbarButton.performClosedEvent();
                    return;
                }
                openNavigationMenu();
            }

            @Override
            public void onCloseRequest() {
                closeNavigationMenu();
            }
        });
        return ret;
    }

    private List<NavigationToolbarDataModel> loadMenuItems() {
        List<NavigationToolbarDataModel> ret = new ArrayList<>();
        int length = LBudgetUtils.getStringArray(mContext, "navigation_items").length;
        for (int i = 0; i < length; i++)
            ret.add(new NavigationToolbarDataModel(mContext, i));
        return ret;
    }

    private void closeNavigationMenu() {
        rotateWedge(Boolean.FALSE);
        Animation animationPushUp = AnimationUtils.loadAnimation(mContext, R.anim.push_down);
        animationPushUp.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float v) {
                return Math.abs(v - 1f); //Reverse the animation so that it actually pushes upwards
            }
        });
        animationPushUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mNavigationMenuView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mNavigationMenuView.startAnimation(animationPushUp);
    }

    private void rotateWedge(Boolean clockwise) {
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

    private void openNavigationMenu() {
        rotateWedge(Boolean.TRUE);
        Animation animationPushDown = AnimationUtils.loadAnimation(mContext, R.anim.push_down);
        animationPushDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mNavigationMenuView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mNavigationMenuView.startAnimation(animationPushDown);
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
