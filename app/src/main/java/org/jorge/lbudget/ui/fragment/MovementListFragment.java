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
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hudomju.swipe.OnItemClickListener;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.SwipeableItemClickListener;
import com.hudomju.swipe.adapter.RecyclerViewAdapter;
import com.hudomju.swipe.adapter.ViewAdapter;

import org.jorge.lbudget.R;
import org.jorge.lbudget.controller.MovementManager;
import org.jorge.lbudget.ui.adapter.MovementListRecyclerAdapter;
import org.jorge.lbudget.ui.component.FloatingActionHideActionBarButton;
import org.jorge.lbudget.util.LBudgetUtils;

public class MovementListFragment extends Fragment implements
        MovementListRecyclerAdapter.MovementImageClickListener, MovementListRecyclerAdapter
        .MovementEditRequestListener {

    private RecyclerView mMovementsView;
    private Context mContext;
    private FloatingActionHideActionBarButton mNewMovementButton;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMovementsView.setLayoutManager(new LinearLayoutManager(mContext));
        mMovementsView.setItemAnimator(new DefaultItemAnimator());
        final MovementListRecyclerAdapter adapter;
        mMovementsView.setAdapter(adapter = new MovementListRecyclerAdapter(view.findViewById
                (android.R.id
                        .empty), mMovementsView, getActivity(), MovementManager.getInstance()
                .getSelectedAccountMovementsToDate(), this, this));
        final SwipeToDismissTouchListener touchListener =
                new SwipeToDismissTouchListener<>(
                        new RecyclerViewAdapter(mMovementsView),
                        new SwipeToDismissTouchListener.DismissCallbacks<ViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return Boolean.TRUE;
                            }

                            @Override
                            public void onDismiss(ViewAdapter viewAdapter, int i) {
                                if (i > -1 && i < adapter.getItemCount()) {
                                    adapter.runDestroy(i);
                                }
                            }
                        }
                );
        mMovementsView.setOnTouchListener(touchListener);
        mMovementsView.setOnScrollListener((RecyclerView.OnScrollListener) touchListener.makeScrollListener());
        mMovementsView.addOnItemTouchListener(new SwipeableItemClickListener(mContext,
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final Integer viewId = view.getId();
                        if (viewId == R.id.txt_delete) {
                            touchListener.processPendingDismisses();
                        } else if (viewId == R.id.txt_undo) {
                            touchListener.undoPendingDismiss();
                            mMovementsView.smoothScrollToPosition(position);
                        } else {
                            adapter.performClick(position);
                        }
                    }
                }
        ));
        mNewMovementButton = (FloatingActionHideActionBarButton) view.findViewById(R.id
                .button_new_item);
        mNewMovementButton.attachToRecyclerView(mMovementsView);
        mNewMovementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateMovementDialog();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View ret = inflater.inflate(R.layout.fragment_movement_list, container, Boolean.FALSE);
        mMovementsView = (RecyclerView) ret.findViewById(R.id.movement_list_view);
        FloatingActionHideActionBarButton newItemButton = (FloatingActionHideActionBarButton) ret
                .findViewById(R.id.button_new_item);
        newItemButton.setTopPadding(mMovementsView.getPaddingTop());
        newItemButton.setActivity(getActivity());
        return ret;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity().getApplicationContext();
        MovementListRecyclerAdapter.updateMovementColors(mContext);
    }

    @Override
    public void onResume() {
        super.onResume();
        MovementListRecyclerAdapter.updateMovementColors(mContext);
        mMovementsView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onMovementImageClick(MovementListRecyclerAdapter.MovementDataModel movement) {
        showMovementImageDialog(movement);
    }

    private void showMovementImageDialog(MovementListRecyclerAdapter.MovementDataModel movement) {
        MovementImageDialogFragment dialogFragment = MovementImageDialogFragment.newInstance
                (mContext, movement);
        dialogFragment.show(getFragmentManager(), LBudgetUtils.getString(mContext,
                "movement_image_dialog_name"));
    }

    private void showCreateMovementDialog() {
        final DialogFragment dialogFragment = (DialogFragment) Fragment.instantiate(mContext,
                MovementDetailDialogFragment.class.getName());
        dialogFragment.show(getFragmentManager(), LBudgetUtils.getString(mContext,
                "new_movement_dialog_name"));
    }

    private void showEditMovementDialog(MovementListRecyclerAdapter.MovementDataModel movement) {
        DialogFragment dialogFragment = MovementDetailDialogFragment.newInstance(mContext,
                movement);
        dialogFragment.show(getFragmentManager(), LBudgetUtils.getString(mContext,
                "edit_movement_dialog_name"));
    }

    @Override
    public void onMovementEditRequested(MovementListRecyclerAdapter.MovementDataModel movement) {
        showEditMovementDialog(movement);
    }
}
