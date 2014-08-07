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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

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
        findViewById(R.id.content_scroll_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_UP == mNavigationToolbarButton.hasBeenOpened()) {
                    mNavigationToolbarButton.initCloseProtocol();
                }
                return Boolean.FALSE;
            }
        });
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
