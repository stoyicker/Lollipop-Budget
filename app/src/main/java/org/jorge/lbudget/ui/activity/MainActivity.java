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

package org.jorge.lbudget.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.jorge.lbudget.R;
import org.jorge.lbudget.ui.adapter.ContentFragmentPagerAdapter;

import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();

        final PagerManager pagerManager = new ContentFragmentPagerAdapter.ContentPagerManager();
        pagerManager.setTitles(context.getResources().getStringArray(R.array.navigation_items));
        final ContentFragmentPagerAdapter contentFragmentPagerAdapter = new
                ContentFragmentPagerAdapter
                (context,
                        getSupportFragmentManager(), pagerManager);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(contentFragmentPagerAdapter);

        final SpringIndicator springIndicator = (SpringIndicator) findViewById(R.id
                .spring_indicator);
        springIndicator.setViewPager(viewPager);
        springIndicator.setOnPageChangeListener(contentFragmentPagerAdapter.makePageChangeListener
                ());
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
