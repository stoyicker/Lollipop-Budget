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

package org.jorge.lbudget.ui.activities;

import android.app.Activity;
import android.app.Fragment;
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

import org.jorge.lbudget.R;
import org.jorge.lbudget.ui.frags.MovementListFragment;
import org.jorge.lbudget.ui.frags.NavigationToolbarFragment;
import org.jorge.lbudget.ui.navbar.NavigationToolbarButton;
import org.jorge.lbudget.ui.navbar.NavigationToolbarRecyclerAdapter;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements NavigationToolbarFragment.NavigationToolbarListener {

    private NavigationToolbarButton mNavigationToolbarButton;
    private RecyclerView mNavigationMenuView;
    private Context mContext;
    private Fragment[] mContentFragments;
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
                    mNavigationToolbarButton.initCloseProtocol();
                    return;
                }
                openNavigationMenu();
            }

            @Override
            public void onCloseRequest() {
                closeNavigationMenu();
            }
        });
        mContentFragments = new Fragment[LBudgetUtils.getStringArray(mContext, "navigation_items").length];
        showInitialFragment();
    }

    private void showInitialFragment() {
        getFragmentManager().beginTransaction().add(R.id.content_fragment_container, findMovementListFragment()).commit();
    }

    private List<NavigationToolbarRecyclerAdapter.NavigationToolbarDataModel> loadMenuItems() {
        List<NavigationToolbarRecyclerAdapter.NavigationToolbarDataModel> ret = new ArrayList<>();
        int length = LBudgetUtils.getStringArray(mContext, "navigation_items").length;
        for (int i = 0; i < length; i++)
            ret.add(new NavigationToolbarRecyclerAdapter.NavigationToolbarDataModel(mContext, i));
        return ret;
    }

    private void closeNavigationMenu() {
        mNavigationToolbarFragment.rotateWedge(Boolean.FALSE);
        mNavigationMenuView.setVisibility(View.GONE);
    }

    private void openNavigationMenu() {
        mNavigationToolbarFragment.rotateWedge(Boolean.TRUE);
        mNavigationMenuView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMenuSelected(int selectedIndex) {
        int oldIndex;
        if ((oldIndex = mNavigationToolbarButton.getSelectedItemPosition()) == selectedIndex)
            return;
        RecyclerView.Adapter adapter = mNavigationMenuView.getAdapter();
        adapter.notifyItemChanged(oldIndex);
        adapter.notifyItemChanged(selectedIndex);
        Fragment target;
        switch (selectedIndex) {
            case 0:
                target = findMovementListFragment();
                break;
            case 1:
                target = findBalanceGraphFragment();
                break;
            case 2:
                target = findAccountsFragment();
                break;
            default:
                throw new IllegalArgumentException("Menu with id " + selectedIndex + " not found.");
        }
        getFragmentManager().beginTransaction().add(R.id.content_fragment_container, target).addToBackStack(null).commit();
    }

    private Fragment findMovementListFragment() {
        if (mContentFragments[0] == null)
            mContentFragments[0] = new MovementListFragment();
        return mContentFragments[0];
    }

    private Fragment findBalanceGraphFragment() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    private Fragment findAccountsFragment() {
        throw new UnsupportedOperationException("Not yet implemented.");
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
                mNavigationToolbarButton.initCloseProtocol();
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
