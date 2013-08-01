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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.fabric3.host.util.FileHelper;
import org.fabric3.monitor.spi.appender.Appender;

/**
 * Writes monitor events to a file that may be rolled periodically according to a {@link RollStrategy}.
 */
public class FileAppender implements Appender {
    private File file;
    private RollStrategy strategy;
    private FileOutputStream stream;
    private FileChannel fileChannel;

    private boolean reliable;

    public FileAppender(File file, RollStrategy strategy, boolean reliable) {
        this.file = file;
        this.strategy = strategy;
        this.reliable = reliable;
    }

    public void start() throws FileNotFoundException {
        initializeChannel();
    }

    public void stop() throws IOException {
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }

    public void write(ByteBuffer buffer) throws IOException {
        roll();
        fileChannel.write(buffer);
        if (reliable) {
            fileChannel.force(false);
        }
    }

    private void initializeChannel() throws FileNotFoundException {
        stream = new FileOutputStream(file, true);
        fileChannel = stream.getChannel();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void roll() throws IOException {
        if (strategy.checkRoll(file)) {
            stream.close();
            File backup = strategy.getBackup(file);
            FileHelper.copyFile(file, backup);
            file.delete();
            initializeChannel();
        }
    }

}
