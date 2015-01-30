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
package org.fabric3.introspection.xml.composite;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 *
 */
public class IncludeLoaderTestCase extends TestCase {

    private LoaderRegistry registry;
    private IncludeLoader loader;
    private XMLStreamReader reader;
    private NamespaceContext namespaceContext;
    private IntrospectionContext context;
    private ClassLoader cl;
    private String namespace;
    private QName name;
    private MetaDataStore store;

    public void testResolveQName() throws Exception {
        expect(reader.getLocation()).andReturn(null).atLeastOnce();
        expect(reader.getAttributeCount()).andReturn(0);
        expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.next()).andReturn(END_ELEMENT);

        expect(context.getTargetNamespace()).andReturn(namespace);
        expect(context.getContributionUri()).andReturn(null);
        QNameSymbol symbol = new QNameSymbol(name);
        Composite include = new Composite(name);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol);
        element.setValue(include);
        expect(store.resolve(EasyMock.isNull(), eq(Composite.class), isA(QNameSymbol.class), isA(IntrospectionContext.class))).andReturn(element);
        replay(registry, reader, namespaceContext, context, store);

        loader.load(reader, context);
        verify(registry, reader, namespaceContext, context, store);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = createMock(LoaderRegistry.class);
        reader = createMock(XMLStreamReader.class);
        namespaceContext = createMock(NamespaceContext.class);
        namespace = "urn:example.com:xmlns";
        context = createMock(IntrospectionContext.class);
        cl = getClass().getClassLoader();
        store = createMock(MetaDataStore.class);
        loader = new IncludeLoader(registry, store);
        name = new QName(namespace, "foo");
    }
}
