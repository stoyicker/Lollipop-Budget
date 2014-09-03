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

package org.jorge.lbudget.logic.controllers;

import org.jorge.lbudget.io.db.SQLiteDAO;
import org.jorge.lbudget.logic.adapters.AccountListRecyclerAdapter;
import org.jorge.lbudget.logic.adapters.MovementListRecyclerAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MovementManager {

    private List<MovementListRecyclerAdapter.MovementDataModel> mMovementList;
    private static MovementManager singleton;

    private MovementManager() {
    }

    public static MovementManager getInstance() {
        if (singleton == null) {
            singleton = new MovementManager();
        }
        return singleton;
    }

    public void setup() {
        mMovementList = SQLiteDAO.getInstance().getSelectedAccountMovementsToDate();
    }

    public List<MovementListRecyclerAdapter.MovementDataModel> getSelectedAccountMovementsToDate() {
        return mMovementList;
    }

    public List<MovementListRecyclerAdapter.MovementDataModel> getAccountMovementsToDate(AccountListRecyclerAdapter.AccountDataModel account) {
        return SQLiteDAO.getInstance().getAccountMovementsToDate(account);
    }

    public Boolean addMovement(MovementListRecyclerAdapter.MovementDataModel movement) {
        Boolean ret = SQLiteDAO.getInstance().addMovement(movement) && !mMovementList.contains(movement);
        if (!mMovementList.contains(movement)) {
            mMovementList.add(movement);
        }
        sortMovementList();
        return ret;
    }

    private void sortMovementList() {
        Collections.sort(mMovementList, new Comparator<MovementListRecyclerAdapter.MovementDataModel>() {
            @Override
            public int compare(MovementListRecyclerAdapter.MovementDataModel movementDataModel1, MovementListRecyclerAdapter.MovementDataModel movementDataModel2) {
                return -((int) (movementDataModel1.getMovementEpoch() - movementDataModel2.getMovementEpoch()));
            }
        });
    }

    public Boolean removeMovement(MovementListRecyclerAdapter.MovementDataModel movement) {
        return SQLiteDAO.getInstance().removeMovement(movement) && mMovementList.remove(movement);
    }

    public Boolean updateMovement(Integer movementId, String newMovementTitle, Long newAmount, Long newEpoch) {
        MovementListRecyclerAdapter.MovementDataModel thisMovement = null;
        for (MovementListRecyclerAdapter.MovementDataModel x : mMovementList)
            if (x.getMovementId() == movementId) {
                thisMovement = x;
                break;
            }

        if (thisMovement == null)
            return Boolean.FALSE;

        sortMovementList();

        thisMovement.setTitle(newMovementTitle);
        thisMovement.setAmount(newAmount);
        thisMovement.setEpoch(newEpoch);
        return SQLiteDAO.getInstance().updateMovement(thisMovement);
    }
}
