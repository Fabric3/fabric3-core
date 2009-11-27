/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.monitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.fabric3.api.annotation.logging.Fine;
import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.LogLevel;
import org.fabric3.api.annotation.logging.LogLevels;
import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.api.annotation.logging.Warning;
import org.fabric3.host.monitor.MonitorFactory;

/**
 * Test case for the JavaLoggingMonitorFactory.
 *
 * @version $Rev$ $Date$
 */
public class JavaLoggingTestCase extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(Monitor.class.getName());
    private static final MockHandler HANDLER = new MockHandler();

    private MonitorFactory factory;

    /**
     * Smoke test to ensure the LOGGER is working.
     */
    public void testLogger() {
        LOGGER.info("test");
        assertEquals(1, HANDLER.logs.size());
    }

    /**
     * Test that no record is logged.
     */
    public void testUnloggedEvent() {
        Monitor mon = factory.getMonitor(Monitor.class);
        mon.eventNotToLog();
        assertEquals(0, HANDLER.logs.size());
    }

    /**
     * Test the correct record is written for an event with no arguments.
     */
    public void testEventWithNoArgs() {
        Monitor mon = factory.getMonitor(Monitor.class);
        mon.eventWithNoArgs();
        assertEquals(1, HANDLER.logs.size());
        LogRecord record = HANDLER.logs.get(0);
        assertEquals(Level.INFO, record.getLevel());
        assertEquals(LOGGER.getName(), record.getLoggerName());
        assertEquals(Monitor.class.getName() + "#eventWithNoArgs", record.getMessage());
    }

    /**
     * Test the correct record is written for an event defined by annotation.
     */
    public void testEventWithInfoAnnotation() {
        Monitor mon = factory.getMonitor(Monitor.class);
        mon.eventWithInfoAnnotation();
        assertEquals(1, HANDLER.logs.size());
        LogRecord record = HANDLER.logs.get(0);
        assertEquals(Level.INFO, record.getLevel());
        assertEquals(LOGGER.getName(), record.getLoggerName());
        assertEquals(Monitor.class.getName() + "#eventWithInfoAnnotation", record.getMessage());
    }

    /**
     * Test the correct record is written for an event defined by annotation.
     */
    public void testEventWithSevereAnnotation() {
        Monitor mon = factory.getMonitor(Monitor.class);
        mon.eventWithSevereAnnotation();
        assertEquals(1, HANDLER.logs.size());
        LogRecord record = HANDLER.logs.get(0);
        assertEquals(Level.SEVERE, record.getLevel());
        assertEquals(LOGGER.getName(), record.getLoggerName());
        assertEquals(Monitor.class.getName() + "#eventWithSevereAnnotation", record.getMessage());
    }

    /**
     * Test the correct record is written for an event defined by annotation.
     */
    public void testEventWithWarningAnnotation() {
        Monitor mon = factory.getMonitor(Monitor.class);
        mon.eventWithWarningAnnotation();
        assertEquals(1, HANDLER.logs.size());
        LogRecord record = HANDLER.logs.get(0);
        assertEquals(Level.WARNING, record.getLevel());
        assertEquals(LOGGER.getName(), record.getLoggerName());
        assertEquals(Monitor.class.getName() + "#eventWithWarningAnnotation", record.getMessage());
    }

    /**
     * Test the correct record is written for an event defined by annotation.
     */
    public void testEventWithFineAnnotation() {
        Monitor mon = factory.getMonitor(Monitor.class);
        mon.eventWithFineAnnotation();
        //Logger log level is info
        assertEquals(0, HANDLER.logs.size());
    }

    /**
     * Test the argument is logged.
     */
    public void testEventWithOneArg() {
        Monitor mon = factory.getMonitor(Monitor.class);
        mon.eventWithOneArg("ARG");
        assertEquals(1, HANDLER.logs.size());
        LogRecord record = HANDLER.logs.get(0);
        assertEquals(Monitor.class.getName() + "#eventWithOneArg", record.getMessage());
    }

    protected void setUp() throws Exception {
        super.setUp();
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(HANDLER);
        HANDLER.flush();

        factory = new JavaLoggingMonitorFactory();
    }

    protected void tearDown() throws Exception {
        LOGGER.removeHandler(HANDLER);
        HANDLER.flush();
        super.tearDown();
    }

    /**
     * Mock log HANDLER to capture records.
     */
    public static class MockHandler extends Handler {
        List<LogRecord> logs = new ArrayList<LogRecord>();

        public void publish(LogRecord record) {
            logs.add(record);
        }

        public void flush() {
            logs.clear();
        }

        public void close() throws SecurityException {
        }
    }

    @SuppressWarnings({"JavaDoc"})
    public static interface Monitor {
        void eventNotToLog();

        @LogLevel(LogLevels.INFO)
        void eventWithNoArgs();

        @LogLevel(LogLevels.INFO)
        void eventWithOneArg(String msg);

        @LogLevel(LogLevels.WARNING)
        void eventWithThrowable(Exception e);

        @Info
        void eventWithInfoAnnotation();

        @Severe
        void eventWithSevereAnnotation();

        @Warning
        void eventWithWarningAnnotation();

        @Fine
        void eventWithFineAnnotation();
    }
}
