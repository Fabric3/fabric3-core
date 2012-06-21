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
package org.fabric3.introspection.xml.definitions;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.host.Namespaces;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev: 7275 $ $Date: 2009-07-05 21:54:59 +0200 (Sun, 05 Jul 2009) $
 */
public class DefinitionsIndexerTestCase extends TestCase {
    private static final QName INTERCEPTED_INTENT = new QName(Namespaces.F3, "intercepted");
    private static final QName QUALIFIER_INTENT = new QName(Namespaces.F3, "qualifier");
    private static final QName PROVIDED_INTENT = new QName(Namespaces.F3, "provided");
    private static final QName PROVIDED_POLICY = new QName(Namespaces.F3, "providedPolicy");
    private static final QName INTERCEPTED_POLICY = new QName(Namespaces.F3, "interceptedPolicy");
    private static final QName WS_POLICY = new QName(Namespaces.F3, "wsPolicy");

    private DefinitionsIndexer loader;
    private XMLStreamReader reader;
    private Set<QName> qNames = new HashSet<QName>();

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public void testIndex() throws Exception {
        Resource resource = new Resource(null, null, "foo");
        IntrospectionContext context = new DefaultIntrospectionContext();
        loader.index(resource, reader, context);

        List<ResourceElement<?, ?>> elements = resource.getResourceElements();
        assertNotNull(elements);
        assertEquals(5, elements.size());
        for (ResourceElement<?, ?> element : elements) {
            Object key = element.getSymbol().getKey();
            assertTrue(qNames.contains(key));
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        loader = new DefinitionsIndexer(null);
        InputStream stream = getClass().getResourceAsStream("definitions.xml");
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        qNames.add(INTERCEPTED_INTENT);
        qNames.add(QUALIFIER_INTENT);
        qNames.add(PROVIDED_INTENT);
        qNames.add(PROVIDED_POLICY);
        qNames.add(INTERCEPTED_POLICY);
        qNames.add(WS_POLICY);
    }
}