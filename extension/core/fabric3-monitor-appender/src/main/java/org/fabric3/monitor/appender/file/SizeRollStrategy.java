/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.monitor.appender.file;

import java.io.File;

/**
 * Signals to roll a file when it has reached a given size.
 */
public class SizeRollStrategy implements RollStrategy {
    private long size;
    private int maxBackups = -1;
    private int counter = 1;

    /**
     * Constructor the size in bytes when a file should be rolled.
     *
     * @param size       the size in bytes when a file should be rolled.
     * @param maxBackups the maximum number of backup files
     */
    public SizeRollStrategy(long size, int maxBackups) {
        this.size = size;
        this.maxBackups = maxBackups;
    }

    public boolean checkRoll(File file) {
        return (file.length() >= size);
    }

    public File getBackup(File file) {
        if (maxBackups > 0) {
            rotateBackups(file);
        }
        while (true) {
            File backup = getLogName(file, counter);
            if (backup.exists()) {
                counter++;
            } else {
                return backup;
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void rotateBackups(File file) {
        if (counter >= maxBackups) {
            // Files need to be rotated. Delete the oldest file.
            getLogName(file, 1).delete();
            // rotate the other log files
            int current = 2;
            while (current > 1) {
                File source = getLogName(file, current);
                current--;
                File target = getLogName(file, current);
                source.renameTo(target);
            }
        }
    }

    private File getLogName(File file, int counter) {
        int pos = file.getName().lastIndexOf(".");
        if (pos < 0) {
            return new File(file.getParent(), file.getName() + counter);
        }
        String name = file.getName().substring(0, pos) + counter;
        String extension = file.getName().substring(pos);
        return new File(file.getParent(), name + extension);
    }

}
