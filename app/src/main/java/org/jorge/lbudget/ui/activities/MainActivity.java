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

package org.jorge.lbudget.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import org.jorge.lbudget.R;
import org.jorge.lbudget.ui.frags.NavigationToolbarFragment;
import org.jorge.lbudget.ui.navbar.NavigationToolbarButton;
import org.jorge.lbudget.ui.navbar.NavigationToolbarDataModel;
import org.jorge.lbudget.ui.navbar.NavigationToolbarRecyclerAdapter;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements NavigationToolbarFragment.NavigationToolbarListener {

    private NavigationToolbarButton mNavigationToolbarButton;
    private RecyclerView mNavigationMenuView;
    private Context mContext;
    private NavigationToolbarFragment mNavigationToolbarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        mNavigationToolbarFragment = (NavigationToolbarFragment) getFragmentManager().findFragmentById(R.id.fragment_navigation_toolbar);
        mNavigationToolbarButton = mNavigationToolbarFragment.getNavigationToolbarButton();
        mNavigationMenuView = (RecyclerView) findViewById(R.id.navigation_toolbar_selector);
        mNavigationMenuView.setHasFixedSize(Boolean.TRUE);
        mNavigationMenuView.setLayoutManager(new LinearLayoutManager(mContext));
        mNavigationMenuView.setItemAnimator(new DefaultItemAnimator());
        mNavigationMenuView.setAdapter(new NavigationToolbarRecyclerAdapter(mContext, loadMenuItems(), mNavigationToolbarFragment, mNavigationToolbarButton));
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
    }


    private List<NavigationToolbarDataModel> loadMenuItems() {
        List<NavigationToolbarDataModel> ret = new ArrayList<>();
        int length = LBudgetUtils.getStringArray(mContext, "navigation_items").length;
        for (int i = 0; i < length; i++)
            ret.add(new NavigationToolbarDataModel(mContext, i));
        return ret;
    }

    private void closeNavigationMenu() {
        mNavigationToolbarFragment.rotateWedge(Boolean.FALSE);
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

    private void openNavigationMenu() {
        mNavigationToolbarFragment.rotateWedge(Boolean.TRUE);
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
    public void onMenuSelected(int selectedIndex) {
        int oldIndex;
        if ((oldIndex = mNavigationToolbarButton.getSelectedItemPosition()) == selectedIndex)
            return;
        RecyclerView.Adapter adapter = mNavigationMenuView.getAdapter();
        adapter.notifyItemChanged(oldIndex);
        adapter.notifyItemChanged(selectedIndex);
        //TODO Perform the fragment transaction
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return Boolean.TRUE;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        startActivity(new Intent(getApplicationContext(), SettingsPreferenceActivity.class));
    }
}
