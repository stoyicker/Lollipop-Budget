package org.jorge.lbudget.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import org.jorge.lbudget.ui.fragment.AccountListFragment;
import org.jorge.lbudget.ui.fragment.ExpenseGraphFragment;
import org.jorge.lbudget.ui.fragment.MovementListFragment;
import org.jorge.lbudget.util.LBudgetUtils;

import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;

public class ContentFragmentPagerAdapter extends ModelPagerAdapter {
    private final Fragment[] mContentFragments;
    private final Context mContext;

    public ContentFragmentPagerAdapter(Context context, FragmentManager fragmentManager,
                                       PagerManager pagerManager) {
        super(fragmentManager, pagerManager);
        mContext = context;
        mContentFragments = new Fragment[LBudgetUtils.getStringArray(mContext,
                "navigation_items").length];
    }


    @Override
    public Fragment getItem(int position) {
        if (mContentFragments[position] == null) {
            final Class c;
            switch (position) {
                case 0:
                    c = MovementListFragment.class;
                    break;
                case 1:
                    c = ExpenseGraphFragment.class;
                    break;
                case 2:
                    c = AccountListFragment.class;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal position " + position);
            }
            mContentFragments[position] = Fragment.instantiate(mContext,
                    c.getName());
        }
        return mContentFragments[position];
    }

    @Override
    public int getCount() {
        return mContentFragments.length;
    }

    public ViewPager.OnPageChangeListener makePageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1)
                    ((ExpenseGraphFragment) mContentFragments[1]).redrawExpenseGraph();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
    }

    public static class ContentPagerManager extends PagerManager {
    }
}
