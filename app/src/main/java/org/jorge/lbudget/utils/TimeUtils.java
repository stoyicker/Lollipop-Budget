package org.jorge.lbudget.utils;

import android.content.Context;

/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public abstract class TimeUtils {
    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long MONTH_MILLIS = 30 * DAY_MILLIS; //Average is accepted
    private static final long YEAR_MILLIS = 12 * MONTH_MILLIS; //No consideration for leap years


    public static String getTimeAgo(long time, Context context) {
        if (time < 1000000000000L) {
            // If the timestamp is given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            throw new IllegalArgumentException("The time provided is either negative or situated in the future.");
        }

        // TODO extend so that it considers years and months as well
        final long diff = now - time;
        String identifier, amount = "";
        if (diff < MINUTE_MILLIS) {
            identifier = "time_ago_just_now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            identifier = "time_ago_a_minute";
        } else if (diff < 50 * MINUTE_MILLIS) {
            identifier = "time_ago_minutes";
            amount = ((int) diff / MINUTE_MILLIS) + "";
        } else if (diff < 90 * MINUTE_MILLIS) {
            identifier = "time_ago_an_hour";
        } else if (diff < DAY_MILLIS) {
            identifier = "time_ago_hours";
            amount = ((int) diff / HOUR_MILLIS) + "";
        } else if (diff < 2 * DAY_MILLIS) {
            identifier = "time_ago_a_day";
        } else if (diff < MONTH_MILLIS) {
            identifier = "time_ago_days";
            amount = ((int) diff / DAY_MILLIS) + "";
        } else if (diff < 2 * MONTH_MILLIS) {
            identifier = "time_ago_a_month";
        } else if (diff < YEAR_MILLIS) {
            identifier = "time_ago_months";
            amount = ((int) diff / MONTH_MILLIS) + "";
        } else if (diff < 2 * YEAR_MILLIS) {
            identifier = "time_ago_a_year";
        } else {
            identifier = "time_ago_years";
            amount = ((int) diff / YEAR_MILLIS) + "";
        }
        return LBudgetUtils.getString(context, identifier).replace(LBudgetUtils.getString(context, "time_ago_placeholder"), amount);
    }
}
