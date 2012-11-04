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
