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
package org.fabric3.introspection.xml.definitions;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class DefinitionsIndexerTestCase extends TestCase {
    private static final QName INTERCEPTED_INTENT = new QName(org.fabric3.api.Namespaces.F3, "intercepted");
    private static final QName QUALIFIER_INTENT = new QName(org.fabric3.api.Namespaces.F3, "qualifier");
    private static final QName PROVIDED_INTENT = new QName(org.fabric3.api.Namespaces.F3, "provided");
    private static final QName PROVIDED_POLICY = new QName(org.fabric3.api.Namespaces.F3, "providedPolicy");
    private static final QName INTERCEPTED_POLICY = new QName(org.fabric3.api.Namespaces.F3, "interceptedPolicy");
    private static final QName WS_POLICY = new QName(org.fabric3.api.Namespaces.F3, "wsPolicy");

    private DefinitionsIndexer loader;
    private XMLStreamReader reader;
    private Set<QName> qNames = new HashSet<>();

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