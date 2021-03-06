package org.jorge.lbudget.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
public abstract class LBudgetTimeUtils {
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
        if (time > now) {
            throw new IllegalArgumentException("The time provided is situated in the future.");
        }
        if (time <= 0)
            throw new IllegalArgumentException("The time provided is negative.");

        final long diff = now - time;
        String identifier, amount = "";
        if (diff < DAY_MILLIS) {
            identifier = "time_ago_today";
        } else if (diff < 2 * DAY_MILLIS) {
            identifier = "time_ago_a_day";
        } else if (diff < MONTH_MILLIS) {
            identifier = "time_ago_days";
            amount = ((int) diff / DAY_MILLIS) + "";
        } else if (diff < 2 * MONTH_MILLIS) {
            identifier = "time_ago_a_month";
        } else if (diff < YEAR_MILLIS) {
            identifier = "time_ago_months";
            amount = (int) (diff / MONTH_MILLIS) + "";
        } else if (diff < 2 * YEAR_MILLIS) {
            identifier = "time_ago_a_year";
        } else {
            identifier = "time_ago_years";
            amount = (int) (diff / YEAR_MILLIS) + "";
        }
        return LBudgetUtils.getString(context, identifier).replace(LBudgetUtils.getString(context, "time_ago_placeholder"), amount);
    }

    public static String epochAsISO8601(Context context, Long epoch) {
        return new SimpleDateFormat(LBudgetUtils.getString(context, "iso_8601_date_format"), Locale.ENGLISH).format(new Date(epoch));
    }

    public static Long ISO8601AsEpoch(Context context, String iso8601) {
        try {
            return new SimpleDateFormat(LBudgetUtils.getString(context, "iso_8601_date_format"), Locale.ENGLISH).parse(iso8601).getTime();
        } catch (ParseException e) {
            Crashlytics.logException(e);
            return null;
        }
    }

    public static String getMonthStringTroughMonthsAgo(Context context, int monthsAgo) {
        if (monthsAgo < 0)
            throw new IllegalArgumentException("Can't calculate movements in the future (monthsAgo is negative)");
        final Long epoch = System.currentTimeMillis() - MONTH_MILLIS * monthsAgo;
        final String epochAsString = LBudgetTimeUtils.epochAsISO8601(context, epoch);
        return LBudgetUtils.getString(context, "month_" + epochAsString.substring(5, 7)) + " " + epochAsString.substring(0, 4);
    }

    public static Long calculateFirstDayOfTheMonthThroughMonthsAgo(Context context, int monthsAgo) {
        if (monthsAgo < 0)
            throw new IllegalArgumentException("Can't calculate movements in the future (monthsAgo is negative)");
        return ISO8601AsEpoch(context, epochAsISO8601(context, System.currentTimeMillis() - MONTH_MILLIS * monthsAgo).substring(0, 8) + "01");
    }

    public static Long calculateFirstDayOfMonthNextTo(Context context, Long initialDayAsEpoch) {
        if (initialDayAsEpoch < 0)
            throw new IllegalArgumentException("Can't calculate last day of a month with a negative epoch");
        Long sum = DAY_MILLIS;
        final String epochAsString = epochAsISO8601(context, initialDayAsEpoch);
        switch (Integer.parseInt(epochAsString.substring(5, 7))) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                sum *= 31;
                break;
            case 2:
                if (Integer.parseInt(epochAsString.substring(0, 4)) % 4 == 0) {
                    sum *= 29;
                } else
                    sum *= 28;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                sum *= 30;
                break;
            default:
                throw new IllegalArgumentException("Month not recognized");
        }
        return initialDayAsEpoch + sum;
    }
}
