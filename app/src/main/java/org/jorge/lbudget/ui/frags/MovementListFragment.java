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
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jorge.lbudget.R;
import org.jorge.lbudget.control.MovementListRecyclerAdapter;
import org.jorge.lbudget.io.db.SQLiteDAO;

public class MovementListFragment extends Fragment {

    private RecyclerView mMovementsView;
    private Context mContext;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMovementsView.setLayoutManager(new LinearLayoutManager(mContext));
        mMovementsView.setItemAnimator(new DefaultItemAnimator());
        mMovementsView.setAdapter(new MovementListRecyclerAdapter(mMovementsView, getActivity(), mContext, SQLiteDAO.loadAccountMovements()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_movement_list, container, Boolean.FALSE);
        mMovementsView = (RecyclerView) ret.findViewById(R.id.movement_list_view);
        return ret;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity().getApplicationContext();
        MovementListRecyclerAdapter.updateMovementColors(mContext);
    }
}
