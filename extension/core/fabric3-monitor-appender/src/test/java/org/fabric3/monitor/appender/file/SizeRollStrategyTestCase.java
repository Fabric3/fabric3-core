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
        FileOutputStream stream = write(logFile, "1234567890");
        stream.close();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void tearDown() throws Exception {
        super.tearDown();
        for (int i = 0; i < 10; i++) {
            new File("f3rolling" + i + ".log").delete();
        }
        logFile.delete();
    }

    private FileOutputStream write(File file, String content) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(content.getBytes());
        return stream;
    }

    private boolean verifyContent(File file, String content) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        return scanner.next().startsWith(content);
    }

}
