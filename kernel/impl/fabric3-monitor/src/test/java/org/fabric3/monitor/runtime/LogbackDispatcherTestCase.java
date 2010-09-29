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
*/
package org.fabric3.monitor.runtime;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.runtime.HostInfo;

/**
 * @version $Rev$ $Date$
 */
public class LogbackDispatcherTestCase extends TestCase {
    private static final String CONFIG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><configuration>" +
                    "   <appender name='CUSTOM' class='org.fabric3.monitor.runtime.TestAppender'>" +
                    "       <encoder><pattern>test-appender: %msg</pattern></encoder>" +
                    "   </appender>" +
                    "   <root level='debug'>" +
                    "       <appender-ref ref='CUSTOM'/>" +
                    "   </root>" +
                    "</configuration>";

    private DocumentBuilder builder;

    public void testConfiguration() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        LogbackDispatcher dispatcher = new LogbackDispatcher("root", info);
        Document doc = builder.parse(new ByteArrayInputStream(CONFIG.getBytes()));
        Element element = doc.getDocumentElement();
        dispatcher.configure(element);
        dispatcher.start();
        dispatcher.onEvent(new MonitorEventImpl("foo", "foo", MonitorLevel.SEVERE, 0, "foo", "this is a test"));
        assertEquals("test-appender: this is a test", TestAppender.getStream().toString());
        dispatcher.stop();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();

    }
}