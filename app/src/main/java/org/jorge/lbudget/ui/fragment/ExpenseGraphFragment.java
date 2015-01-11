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

package org.jorge.lbudget.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.jorge.lbudget.R;
import org.jorge.lbudget.controller.MovementManager;
import org.jorge.lbudget.util.LBudgetTimeUtils;
import org.jorge.lbudget.util.LBudgetUtils;

import java.util.List;

public class ExpenseGraphFragment extends Fragment {

    private Context mContext;
    private int MONTHS_AGO = 0;
    private View mNoMovementsView;
    private TextView mTitleView;
    private PieChart mPieChart;

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

        mNoMovementsView = view.findViewById(android.R.id.empty);

        mTitleView = (TextView) view.findViewById(R.id.expense_graph_month_title_view);
        mTitleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMonthChooserDialog();
            }
        });

        mPieChart = (PieChart) view.findViewById(R.id.expense_chart);

        redrawExpenseGraph();
    }

    private synchronized void redrawExpenseGraph() {

        final int monthsAgo = MONTHS_AGO;

        mTitleView.setText(LBudgetTimeUtils.getMonthStringTroughMonthsAgo(mContext, monthsAgo));

        int maxUniquePies = 1;
        while (LBudgetUtils.getColor(mContext, "expense_type_" + maxUniquePies + "_color") != -1) {
            maxUniquePies++;
        }
        maxUniquePies -= 2; //One for the initial index, another one because the last color belongs to 'Other'

        List<PieModel> pies = MovementManager.getInstance().createMonthlyPieModels(mContext, monthsAgo, maxUniquePies);

        if (pies.isEmpty()) {
            mPieChart.setVisibility(View.GONE);
            mNoMovementsView.setVisibility(View.VISIBLE);
        } else {
            for (PieModel pie : pies) {
                mPieChart.addPieSlice(pie);
            }
            mPieChart.startAnimation();
            mPieChart.setVisibility(View.VISIBLE);
            mNoMovementsView.setVisibility(View.GONE);
        }
    }

    private void showMonthChooserDialog() {
        AlertDialog.Builder monthChooserBuilder = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                R.layout.select_dialog_singlechoice_black);
        int j = 0;
        for (int i = 0; i < 12; i++) {
            String month = LBudgetTimeUtils.getMonthStringTroughMonthsAgo(mContext, j);
            if (LBudgetUtils.adapterContains(adapter, month)) {
                j++;
                month = LBudgetTimeUtils.getMonthStringTroughMonthsAgo(mContext, j);
            }
            adapter.add(month);
            j++;
        }

        monthChooserBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        monthChooserBuilder.setSingleChoiceItems(adapter, MONTHS_AGO, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (MONTHS_AGO < 0 || MONTHS_AGO > 11)
                    return;
                ExpenseGraphFragment.this.MONTHS_AGO = i;
                ExpenseGraphFragment.this.redrawExpenseGraph();
                dialogInterface.dismiss();
            }
        });

        monthChooserBuilder.show();
    }
}
