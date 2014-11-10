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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.system.introspection;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.model.type.system.SystemImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationIntrospector;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 *
 */
public class SystemImplementationLoaderTestCase extends TestCase {
    private IntrospectionContext context;
    private XMLStreamReader reader;
    private ImplementationIntrospector implementationIntrospector;
    private SystemImplementationLoader loader;

    public void testLoad() throws Exception {
        implementationIntrospector.introspect(EasyMock.isA(InjectingComponentType.class), EasyMock.eq(context));

        EasyMock.replay(implementationIntrospector);

        EasyMock.expect(reader.getLocation()).andReturn(null).atLeastOnce();
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "class")).andReturn(getClass().getName());
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);

        SystemImplementation impl = loader.load(reader, context);
        assertEquals(getClass().getName(), impl.getImplementationClass());
        EasyMock.verify(reader);
        EasyMock.verify(context);
        EasyMock.verify(implementationIntrospector);
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        implementationIntrospector = EasyMock.createMock(ImplementationIntrospector.class);

        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.replay(context);

        reader = EasyMock.createMock(XMLStreamReader.class);

        loader = new SystemImplementationLoader(implementationIntrospector);
    }
}
