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
import java.util.Scanner;

import junit.framework.TestCase;

/**
 *
 */
public class SizeRollStrategyTestCase extends TestCase {
    private File logFile;

    public void testTriggerRoll() throws Exception {
        SizeRollStrategy strategy = new SizeRollStrategy(10, -1);
        assertTrue(strategy.checkRoll(logFile));
    }

    public void testMaxBackups() throws Exception {
        SizeRollStrategy strategy = new SizeRollStrategy(10, 2);
        File backup1 = new File("f3rolling1.log");
        File backup2 = new File("f3rolling2.log");
        File backup3 = new File("f3rolling3.log");

        File currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "A234567890");

        assertTrue(backup1.exists());
        assertFalse(backup2.exists());
        assertFalse(backup3.exists());

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "B234567890");

        assertTrue(backup1.exists());
        assertTrue(verifyContent(backup1, "A"));

        assertTrue(backup2.exists());
        assertTrue(verifyContent(backup2, "B"));

        assertFalse(backup3.exists());

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "C234567890");

        assertTrue(backup1.exists());
        assertTrue(verifyContent(backup1, "B"));

        assertTrue(backup2.exists());
        assertTrue(verifyContent(backup2, "C"));

        assertFalse(backup3.exists());

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testMaxBackupsFileDeleted() throws Exception {
        SizeRollStrategy strategy = new SizeRollStrategy(10, 2);
        File backup1 = new File("f3rolling1.log");
        File backup2 = new File("f3rolling2.log");
        File backup3 = new File("f3rolling3.log");

        File currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "A234567890");

        assertTrue(backup1.exists());
        assertFalse(backup2.exists());
        assertFalse(backup3.exists());

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "B234567890");

        assertTrue(backup1.exists());
        assertTrue(verifyContent(backup1, "A"));

        assertTrue(backup2.exists());
        assertTrue(verifyContent(backup2, "B"));

        assertFalse(backup3.exists());

        backup2.delete();  // delete the file

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "C234567890");

        assertTrue(backup1.exists());

        assertFalse(backup2.exists());
        assertTrue(verifyContent(backup1, "C"));

        assertFalse(backup3.exists());

    }

    public void testRollReset() throws Exception {
        SizeRollStrategy strategy = new SizeRollStrategy(10, 2);
        File backup1 = new File("f3rolling1.log");
        File backup2 = new File("f3rolling2.log");
        File backup3 = new File("f3rolling3.log");

        File currentBackup = strategy.getBackup(logFile);

        write(currentBackup, "A234567890");

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "B234567890");

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "C234567890");

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "D234567890");

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "E234567890");

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "F234567890");

        currentBackup = strategy.getBackup(logFile);
        write(currentBackup, "G234567890");

        assertTrue(backup1.exists());
        assertTrue(verifyContent(backup1, "F"));

        assertTrue(backup2.exists());
        assertTrue(verifyContent(backup2, "G"));

        assertFalse(backup3.exists());

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void setUp() throws Exception {
        super.setUp();
        for (int i = 0; i < 10; i++) {
            new File("f3rolling" + i + ".log").delete();
        }
        logFile = new File("f3rolling.log");
        write(logFile, "1234567890");

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void tearDown() throws Exception {
        super.tearDown();
        for (int i = 0; i < 10; i++) {
            new File("f3rolling" + i + ".log").delete();
        }
        logFile.delete();
    }

    private void write(File file, String content) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(content.getBytes());
        }

    }

    private boolean verifyContent(File file, String content) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(file)) {
            return scanner.next().startsWith(content);
        }
    }

}
