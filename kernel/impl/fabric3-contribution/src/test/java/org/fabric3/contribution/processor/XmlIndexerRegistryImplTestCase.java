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
package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlIndexer;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class XmlIndexerRegistryImplTestCase extends TestCase {
    private static final QName TYPE = new QName("test", "element");

    public void testIndex() throws Exception {
        XmlIndexer indexer = EasyMock.createMock(XmlIndexer.class);
        EasyMock.expect(indexer.getType()).andReturn(TYPE);
        indexer.index(EasyMock.isA(Resource.class), EasyMock.isA(XMLStreamReader.class), EasyMock.isA(IntrospectionContext.class));

        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getName()).andReturn(TYPE);

        EasyMock.replay(indexer, reader);

        Resource resource = new Resource(null, null, null);

        XmlIndexerRegistryImpl registry = new XmlIndexerRegistryImpl();
        registry.register(indexer);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        registry.index(resource, reader, context);

        registry.unregister(TYPE);
        EasyMock.verify(indexer, reader);
    }

}
