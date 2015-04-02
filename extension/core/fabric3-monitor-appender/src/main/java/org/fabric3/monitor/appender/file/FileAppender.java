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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.util.FileHelper;
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

    public void start() {
        initializeChannel();
    }

    public void stop() {
        try {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }

    public synchronized void write(ByteBuffer buffer) {
        try {
            roll();
            fileChannel.write(buffer);
            if (reliable) {
                fileChannel.force(false);
            }
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }

    private void initializeChannel() {
        try {
            stream = new FileOutputStream(file, true);
            fileChannel = stream.getChannel();
        } catch (FileNotFoundException e) {
            throw new Fabric3Exception(e);
        }
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
