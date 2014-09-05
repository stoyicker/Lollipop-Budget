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

package org.jorge.lbudget.ui.frags;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.jorge.lbudget.R;
import org.jorge.lbudget.logic.controllers.MovementManager;
import org.jorge.lbudget.utils.LBudgetTimeUtils;

import java.util.List;

public class ExpenseGraphFragment extends Fragment {

    private Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expense_graph, container, Boolean.FALSE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO Put here some values
        //TODO Try to use the viewpager to see previous months

        int monthsAgo = 0;

        ((TextView) view.findViewById(R.id.expense_graph_month_view)).setText(LBudgetTimeUtils.getMonthStringInMonthsAgo(mContext, monthsAgo));

        PieChart mPieChart = (PieChart) view.findViewById(R.id.expense_chart);

        List<PieModel> pies = MovementManager.getInstance().createPies(monthsAgo, 5);

        for (PieModel pie : pies) {
            mPieChart.addPieSlice(pie);
        }

        mPieChart.addPieSlice(new PieModel("Sleep", 25, Color.parseColor("#56B7F1")));
        mPieChart.addPieSlice(new PieModel("Work", 35, Color.parseColor("#CDA67F")));
        mPieChart.addPieSlice(new PieModel("Eating", 9, Color.parseColor("#FED70E")));

        mPieChart.startAnimation();
    }
}
