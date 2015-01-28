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
package org.fabric3.security.impl;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.security.BasicSecuritySubject;

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

        HostInfo info = EasyMock.createNiceMock(HostInfo.class);

        FileSecurityStore store = new FileSecurityStore(info);
        store.setSecurityConfiguration(reader);

        BasicSecuritySubject foo = store.find("foo");
        assertEquals("bar", foo.getPassword());
        BasicSecuritySubject baz = store.find("baz");
        assertEquals("fred", baz.getPassword());
        assertEquals(2, foo.getRoles().size());
        assertEquals(2, baz.getRoles().size());
    }
}
