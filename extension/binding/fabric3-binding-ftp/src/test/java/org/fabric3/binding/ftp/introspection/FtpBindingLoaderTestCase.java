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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.ftp.introspection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.binding.ftp.model.FtpBindingDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 *
 */
public class FtpBindingLoaderTestCase extends TestCase {
    private static final String XML_NO_COMMANDS =
            "<f3:binding.ftp uri=\"ftp://foo.com/service\" xmlns:f3=\"urn:fabric3.org\"></f3:binding.ftp>";

    private static final String XML_COMMANDS =
            "<f3:binding.ftp uri=\"ftp://foo.com/service\" xmlns:f3=\"urn:fabric3.org\">\n" +
                    "   <commands>\n" +
                    "     <command>QUOTE test1</command>\n" +
                    "     <command>QUOTE test2</command>\n" +
                    "   </commands>\n" +
                    "</f3:binding.ftp>";

    private DefaultIntrospectionContext context;
    private FtpBindingLoader loader;

    public void testBindingNoCommandsParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML_NO_COMMANDS.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        FtpBindingDefinition definition = loader.load(reader, context);
        assertNotNull(definition.getTargetUri());
    }

    public void testBindingCommandsParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML_COMMANDS.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        FtpBindingDefinition definition = loader.load(reader, context);
        List<String> commands = definition.getSTORCommands();
        assertEquals(2, commands.size());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = new DefaultIntrospectionContext();

        LoaderHelper helper = EasyMock.createNiceMock(LoaderHelper.class);
        EasyMock.replay(helper);
        loader = new FtpBindingLoader(helper);

    }
}
