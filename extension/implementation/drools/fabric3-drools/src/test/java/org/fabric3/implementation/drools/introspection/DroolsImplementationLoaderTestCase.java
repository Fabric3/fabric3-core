/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.implementation.drools.introspection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.implementation.drools.model.DroolsImplementation;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class DroolsImplementationLoaderTestCase extends TestCase {

    private static final String XML = "<implementation.drools><resource source='HelloWorld.drl'/></implementation.drools>";

    private static final String XML_NO_RESOURCES = "<implementation.drools></implementation.drools>";

    private XMLInputFactory xmlFactory;
    private DefaultIntrospectionContext context;
    private DroolsImplementationLoader loader;
    private RulesIntrospector rulesIntrospector;

    @SuppressWarnings({"unchecked"})
    public void testParse() throws Exception {
        ComponentType componentType = new ComponentType();
        EasyMock.expect(rulesIntrospector.introspect(EasyMock.isA(Map.class),
                                                     EasyMock.isA(Map.class),
                                                     EasyMock.isA(IntrospectionContext.class))).andReturn(componentType);
        EasyMock.replay(rulesIntrospector);
        XMLStreamReader reader = createReader(XML);
        DroolsImplementation implementation = loader.load(reader, context);
        assertTrue(context.getErrors().isEmpty());
        assertEquals(1, implementation.getPackages().size());
        EasyMock.verify(rulesIntrospector);
    }

    public void testNoResource() throws Exception {
        EasyMock.replay(rulesIntrospector);
        XMLStreamReader reader = createReader(XML_NO_RESOURCES);
        loader.load(reader, context);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingKnowledgeBaseDefinition);
        EasyMock.verify(rulesIntrospector);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xmlFactory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext(URI.create("test"), getClass().getClassLoader());
        rulesIntrospector = EasyMock.createMock(RulesIntrospector.class);
        loader = new DroolsImplementationLoader(rulesIntrospector);
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        InputStream in = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(in);
        reader.nextTag();
        return reader;
    }

}
