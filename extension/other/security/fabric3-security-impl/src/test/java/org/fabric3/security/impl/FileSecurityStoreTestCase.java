/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.security.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.xml.XMLFactory;

/**
 *
 */
public class FileSecurityStoreTestCase extends TestCase {

    private static final String XML =
            "        <users>" +
                    "   <user>" +
                    "      <username>foo</username>" +
                    "      <password>bar</password>" +
                    "      <roles>" +
                    "         <role>role1</role>" +
                    "         <role>role2</role>" +
                    "       </roles>" +
                    "   </user>" +
                    "   <user>" +
                    "      <username>baz</username>" +
                    "      <password>fred</password>" +
                    "      <roles>" +
                    "         <role>role3</role>" +
                    "         <role>role4</role>" +
                    "       </roles>" +
                    "   </user>" +
                    "</users>";

    public void testParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();

        XMLFactory factory = EasyMock.createNiceMock(XMLFactory.class);
        EasyMock.replay(factory);
        HostInfo info = EasyMock.createNiceMock(HostInfo.class);

        FileSecurityStore store = new FileSecurityStore(factory, info);
        store.setSecurityConfiguration(reader);

        BasicSecuritySubject foo = store.find("foo");
        assertEquals("bar", foo.getPassword());
        BasicSecuritySubject baz = store.find("baz");
        assertEquals("fred", baz.getPassword());
        assertEquals(2, foo.getRoles().size());
        assertEquals(2, baz.getRoles().size());
    }
}
