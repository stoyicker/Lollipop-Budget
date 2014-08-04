/*
 * This file is part of LBudget.
 * LBudget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * LBudget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with LBudget. If not, see <http://www.gnu.org/licenses/>.
 */

package org.jorge.lbudget.devutils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jorge.lbudget.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

@SuppressWarnings({"UnusedDeclaration"})
public abstract class DevUtils {

    public static void showTrace(String tag, Exception source) {
        if (!BuildConfig.DEBUG) return;
        StackTraceElement[] trace = source.getStackTrace();
        String toPrint = "";
        for (StackTraceElement x : trace) {
            toPrint += "Class " + x.getClassName() + " -  " + x.getMethodName() + ":" +
                    x.getLineNumber();
            toPrint += "\n";
        }
        Log.d(tag, toPrint);
    }

    public static void writeToFile(String data, Context context, String fileName) {
        if (!BuildConfig.DEBUG) return;
        File f;
        if ((f = new File(fileName)).exists())
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(
                            context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public static void debugSelectAllFromTable(SQLiteDatabase readableDatabase, String tag, String[] fields, String tableName) {
        if (!BuildConfig.DEBUG) return;

        readableDatabase.beginTransaction();
        Cursor cursor = readableDatabase.query(tableName, fields, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Log.d(tag, fields[0] + ": " + cursor.getInt(0));
            Log.d(tag, fields[1] + ": " + cursor.getString(1));
            Log.d(tag, fields[2] + ": " + cursor.getString(2));
            Log.d(tag, fields[3] + ": " + cursor.getString(3));
            Log.d(tag, fields[4] + ": " + cursor.getString(4));
            Log.d(tag, fields[5] + ": " + Arrays.toString(cursor.getBlob(5)));
        }
        cursor.close();
        readableDatabase.setTransactionSuccessful();
        readableDatabase.endTransaction();
    }

    public static void logArray(String tag, String arrayName, Object[] array) {
        if (!BuildConfig.DEBUG) return;
        Log.d(tag, "Logging array " + arrayName);
        for (Object x : array)
            Log.d(tag, x + "\n");
    }

    public static void logString(String tag, String msg) {
        if (!BuildConfig.DEBUG) return;
        Log.d(tag, msg);
    }
}
