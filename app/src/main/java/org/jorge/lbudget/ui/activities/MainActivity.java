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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.jorge.lbudget.R;
import org.jorge.lbudget.ui.custom.NavigationToolbarSpinner;
import org.jorge.lbudget.ui.frags.NavigationToolbarFragment;

public class MainActivity extends Activity implements NavigationToolbarFragment.NavigationToolbarListener {

    private NavigationToolbarSpinner mNavigationToolbarSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNavigationToolbarSelector = ((NavigationToolbarFragment) getFragmentManager().findFragmentById(R.id.fragment_navigation_toolbar)).getNavigationSpinner();
    }

    @Override
    public void onMenuSelected(int index) {
        if (mNavigationToolbarSelector.getSelectedItemPosition() == index)
            return;
        //TODO Perform the fragment transaction
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mNavigationToolbarSelector.hasBeenOpened() && hasFocus) {
            mNavigationToolbarSelector.performClosedEvent();
        }
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
