/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.composite;

import java.net.URI;
import java.net.URL;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.stream.Source;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Include;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @version $Rev: 7275 $ $Date: 2009-07-05 21:54:59 +0200 (Sun, 05 Jul 2009) $
 */
public class IncludeLoaderTestCase extends TestCase {

    private LoaderRegistry registry;
    private IncludeLoader loader;
    private XMLStreamReader reader;
    private NamespaceContext namespaceContext;
    private IntrospectionContext context;
    private URL includeURL;
    private ClassLoader cl;
    private String namespace;
    private QName name;
    private MetaDataStore store;
    private Composite composite;

    public void testResolveQName() throws Exception {
        expect(reader.getAttributeCount()).andReturn(0);
        expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.getAttributeValue(null, "scdlResource")).andReturn(null);
        expect(reader.next()).andReturn(END_ELEMENT);

        expect(context.getTargetNamespace()).andReturn(namespace);
        expect(context.getClassLoader()).andReturn(cl);
        expect(context.getContributionUri()).andReturn(null);
        QNameSymbol symbol = new QNameSymbol(name);
        Composite include = new Composite(name);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
        element.setValue(include);
        // FIXME null check
        expect(store.resolve((URI) EasyMock.isNull(),
                             eq(Composite.class),
                             isA(QNameSymbol.class),
                             isA(IntrospectionContext.class))).andReturn(element);
        replay(registry, reader, namespaceContext, context, store);

        loader.load(reader, context);
        verify(registry, reader, namespaceContext, context, store);
    }

    public void testWithScdlResource() throws LoaderException, XMLStreamException {
        String resource = "org/fabric3/introspection/xml/composite/test-include.composite";
        includeURL = cl.getResource(resource);
        assertNotNull(includeURL);

        expect(reader.getAttributeCount()).andReturn(0);
        expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.getAttributeValue(null, "scdlResource")).andReturn(resource);
        expect(reader.next()).andReturn(END_ELEMENT);

        expect(context.getTargetNamespace()).andReturn(namespace);
        expect(context.getClassLoader()).andReturn(cl);
        expect(context.getContributionUri()).andReturn(null);

        expect(registry.load(isA(Source.class), eq(Composite.class), isA(IntrospectionContext.class))).andReturn(composite);
        replay(registry, reader, namespaceContext, context, store);

        Include include = loader.load(reader, context);
        assertEquals(name, include.getName());
        assertEquals(includeURL, include.getScdlLocation());
        verify(registry, reader, namespaceContext, context);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = createMock(LoaderRegistry.class);
        reader = createMock(XMLStreamReader.class);
        namespaceContext = createMock(NamespaceContext.class);
        namespace = "urn:example.com:xmlns";
        context = createMock(IntrospectionContext.class);
        cl = getClass().getClassLoader();
        includeURL = new URL("file:/include.scdl");
        store = createMock(MetaDataStore.class);
        loader = new IncludeLoader(registry, store);
        name = new QName(namespace, "foo");
        composite = new Composite(null);
    }
}
