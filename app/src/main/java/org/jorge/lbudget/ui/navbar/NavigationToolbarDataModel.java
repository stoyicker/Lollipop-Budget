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

package org.jorge.lbudget.ui.navbar;

import android.content.Context;

import org.jorge.lbudget.utils.LBudgetUtils;

public class NavigationToolbarDataModel {
    private final int iconResId;

    public String getText() {
        return text;
    }

    public int getIconResId() {
        return iconResId;
    }

    private String text;

    public NavigationToolbarDataModel(Context context, int menuId) {
        iconResId = LBudgetUtils.getDrawableAsId("ic_navigation_menu" + menuId);
        text = LBudgetUtils.getStringArray(context, "navigation_items")[menuId];
    }
}
