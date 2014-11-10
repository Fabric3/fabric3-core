/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
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
            int current = 1;
            while (current < counter) {
                File target = getLogName(file, current);
                current++;
                File source = getLogName(file, current);
                source.renameTo(target);
            }
            counter = 1;
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
