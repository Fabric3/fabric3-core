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
package org.fabric3.monitor.impl.appender;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.util.FileHelper;

/**
 *
 */
public class AppenderFactoryImplTestCase extends TestCase {
    private static final String CONSOLE_APPENDER = "<appenders><appender type='console'/></appenders>";
    private static final String TWO_APPENDERS = "<appenders><appender type='console'/><appender type='console'/></appenders>";
    private static final String ROLLING_APPENDER_NO_STRATEGY = "<appenders><appender type='file'/></appenders>";
    private static final String ROLLING_APPENDER_SIZE_STRATEGY = "<appenders><appender type='file' roll.type='size' roll.size='10'/></appenders>";

    private AppenderFactory factory;
    private HostInfo hostInfo;
    private AppenderFactoryMonitor monitor;
    private File file;

    public void testCreateConsoleAppender() throws Exception {
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(CONSOLE_APPENDER.getBytes()));

        List<Appender> appenders = factory.instantiate(reader);
        assertTrue(appenders.get(0) instanceof ConsoleAppender);
    }

    public void testCreateFileAppenderNoRoll() throws Exception {
        hostInfo.getDataDir();
        EasyMock.expectLastCall().andReturn(file);
        EasyMock.replay(hostInfo);
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(ROLLING_APPENDER_NO_STRATEGY.getBytes()));

        List<Appender> appenders = factory.instantiate(reader);
        assertTrue(appenders.get(0) instanceof RollingFileAppender);

        EasyMock.verify(hostInfo);
    }

    public void testCreateFileAppenderSizeRoll() throws Exception {
        hostInfo.getDataDir();
        EasyMock.expectLastCall().andReturn(file);
        EasyMock.replay(hostInfo);
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(ROLLING_APPENDER_SIZE_STRATEGY.getBytes()));

        List<Appender> appenders = factory.instantiate(reader);
        assertTrue(appenders.get(0) instanceof RollingFileAppender);

        EasyMock.verify(hostInfo);
    }

    public void testCreateTwoAppenders() throws Exception {
        monitor.multipleAppenders("console");
        EasyMock.replay(monitor);

        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(TWO_APPENDERS.getBytes()));

        factory.instantiate(reader);
        EasyMock.verify(monitor);
    }

    public void setUp() throws Exception {
        super.setUp();
        file = new File("tmp");
        hostInfo = EasyMock.createMock(HostInfo.class);
        monitor = EasyMock.createMock(AppenderFactoryMonitor.class);

        factory = new AppenderFactoryImpl(hostInfo, monitor);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(file);
    }
}
