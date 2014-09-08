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

package org.jorge.lbudget.io.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class FileManager {
    public static Boolean recursiveDelete(File file) {
        if (file == null || !file.exists()) return Boolean.FALSE;
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String aChildren : children) {
                Boolean success = recursiveDelete(new File(file, aChildren));
                if (!success) {
                    return false;
                }
            }
        }

        return file.delete();
    }

    public static String readFileAsString(File target)
            throws IOException {

        char[] buff = new char[1024];
        StringBuilder builder = new StringBuilder();

        FileReader reader = new FileReader(target);

        while (reader.read(buff) != -1) {
            builder.append(buff);
        }

        reader.close();

        return builder.toString();
    }

    public static Boolean writeStringToFile(String string, File file, Boolean append)
            throws IOException {

        if (!file.exists()) {
            if (!file.createNewFile())
                return Boolean.FALSE;
        }

        FileOutputStream outputStream = new FileOutputStream(file, append);
        outputStream.write(string.getBytes(Charset.defaultCharset()));
        outputStream.close();

        return Boolean.TRUE;
    }
}
