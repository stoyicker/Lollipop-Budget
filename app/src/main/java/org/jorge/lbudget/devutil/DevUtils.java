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

package org.jorge.lbudget.devutil;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jorge.lbudget.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

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
            Crashlytics.logException(e);
        }
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
