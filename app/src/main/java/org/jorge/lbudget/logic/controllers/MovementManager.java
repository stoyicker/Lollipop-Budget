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

package org.jorge.lbudget.logic.controllers;

import android.content.Context;
import android.os.Environment;

import org.eazegraph.lib.models.PieModel;
import org.jorge.lbudget.io.db.SQLiteDAO;
import org.jorge.lbudget.io.files.FileManager;
import org.jorge.lbudget.logic.adapters.AccountListRecyclerAdapter;
import org.jorge.lbudget.logic.adapters.MovementListRecyclerAdapter;
import org.jorge.lbudget.utils.LBudgetTimeUtils;
import org.jorge.lbudget.utils.LBudgetUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
        if (movement.getMovementEpoch() < System.currentTimeMillis()) {
            mMovementList.add(movement);
            sortMovementList();
        } else
            ret = Boolean.FALSE;
        return ret;
    }

    private void sortMovementList() {
        Collections.sort(mMovementList, new Comparator<MovementListRecyclerAdapter.MovementDataModel>() {
            @Override
            public int compare(MovementListRecyclerAdapter.MovementDataModel movementDataModel1, MovementListRecyclerAdapter.MovementDataModel movementDataModel2) {
                return movementDataModel1.getMovementEpoch() < movementDataModel2.getMovementEpoch() ? 1 : -1;
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

    public List<PieModel> createMonthlyPieModels(Context context, int monthsAgo, int maxUniquePies) {
        if (monthsAgo < 0)
            throw new IllegalArgumentException("Can't calculate movements in the future (monthsAgo is negative)");

        final Long firstDay = LBudgetTimeUtils.calculateFirstDayOfTheMonthThroughMonthsAgo(context, monthsAgo), lastDay = LBudgetTimeUtils.calculateFirstDayOfMonthNextTo(context, firstDay);

        List<MovementListRecyclerAdapter.MovementDataModel> expensesInMonth = MovementManager.getInstance().getSelectedAccountExpensesBetween(firstDay, lastDay);
        Collections.sort(expensesInMonth, new Comparator<MovementListRecyclerAdapter.MovementDataModel>() {
            @Override
            public int compare(MovementListRecyclerAdapter.MovementDataModel movementDataModel1, MovementListRecyclerAdapter.MovementDataModel movementDataModel2) {
                return movementDataModel1.getMovementAmount().compareTo(movementDataModel2.getMovementAmount());
            }
        });

        MovementListRecyclerAdapter.MovementDataModel cumulativeMovement = null;
        int colorCounter = 1;
        final String colorString = "expense_type_{PLACEHOLDER}_color";
        List<PieModel> ret = new ArrayList<>();
        while (!expensesInMonth.isEmpty()) {
            if (cumulativeMovement == null) {
                cumulativeMovement = expensesInMonth.remove(0);
                if (expensesInMonth.isEmpty())
                    ret.add(new PieModel(cumulativeMovement.getMovementTitle(), Math.abs(cumulativeMovement.getMovementAmount()) / 100, LBudgetUtils.getColor(context, colorString.replace("{PLACEHOLDER}", Math.min(colorCounter, maxUniquePies) + ""))));
            } else {
                MovementListRecyclerAdapter.MovementDataModel newMovement = expensesInMonth.remove(0);
                if (cumulativeMovement.getMovementTitle().toLowerCase(Locale.ENGLISH).contentEquals(newMovement.getMovementTitle().toLowerCase(Locale.ENGLISH))) {
                    cumulativeMovement = new MovementListRecyclerAdapter.MovementDataModel(-1, LBudgetUtils.capitalizeFirst(cumulativeMovement.getMovementTitle()).toString(), cumulativeMovement.getMovementAmount() + newMovement.getMovementAmount(), cumulativeMovement.getMovementEpoch());//The id and the epoch are useless but we need the wrapper
                } else {
                    ret.add(new PieModel(cumulativeMovement.getMovementTitle(), Math.abs(cumulativeMovement.getMovementAmount()) / 100, LBudgetUtils.getColor(context, colorString.replace("{PLACEHOLDER}", Math.min(colorCounter, maxUniquePies) + ""))));
                    cumulativeMovement = newMovement;
                    colorCounter++;
                }
                if (expensesInMonth.isEmpty()) {
                    ret.add(new PieModel(cumulativeMovement.getMovementTitle(), Math.abs(cumulativeMovement.getMovementAmount()) / 100, LBudgetUtils.getColor(context, colorString.replace("{PLACEHOLDER}", Math.min(colorCounter, maxUniquePies) + ""))));
                    colorCounter++;
                }
            }
            if (colorCounter > maxUniquePies)
                break;
        }
        if (colorCounter > maxUniquePies && !expensesInMonth.isEmpty()) {
            cumulativeMovement = null;
            while (!expensesInMonth.isEmpty()) {
                if (cumulativeMovement == null) {
                    cumulativeMovement = expensesInMonth.remove(0);
                } else
                    cumulativeMovement.setAmount(cumulativeMovement.getMovementAmount() + expensesInMonth.remove(0).getMovementAmount());
            }
            assert cumulativeMovement != null;
            ret.add(new PieModel(LBudgetUtils.getString(context, "other_movements_pie_title"), Math.abs(cumulativeMovement.getMovementAmount()) / 100, LBudgetUtils.getColor(context, colorString.replace("{PLACEHOLDER}", maxUniquePies + 1 + ""))));
        }
        return ret;
    }

    private List<MovementListRecyclerAdapter.MovementDataModel> getSelectedAccountExpensesBetween(Long lowestExtreme, Long highestExtreme) {
        List<MovementListRecyclerAdapter.MovementDataModel> ret = new ArrayList<>();
        for (MovementListRecyclerAdapter.MovementDataModel x : mMovementList) {
            Long epoch = x.getMovementEpoch();
            if (lowestExtreme <= epoch && epoch <= highestExtreme && x.getMovementAmount() < 0) {
                ret.add(x);
            }
        }
        return ret;
    }

    public synchronized Boolean exportMovementsAsCSV(Context context) {
        final List<AccountListRecyclerAdapter.AccountDataModel> allAccs = SQLiteDAO.getInstance().getAccounts();

        final File target = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), LBudgetUtils.getString(context, "exported_file_name"));

        FileManager.recursiveDelete(target);

        for (AccountListRecyclerAdapter.AccountDataModel acc : allAccs) {
            List<MovementListRecyclerAdapter.MovementDataModel> allMovs = SQLiteDAO.getInstance().getAllMovementsInAccount(acc);

            final StringBuilder thisAccount = new StringBuilder(acc.toCsvString() + "\n");
            for (MovementListRecyclerAdapter.MovementDataModel mov : allMovs) {
                thisAccount.append(mov.toCsvString()).append("\n");
            }

            thisAccount.append("\n");

            try {
                FileManager.writeStringToFile(thisAccount.toString(), target, Boolean.TRUE);
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }
}
