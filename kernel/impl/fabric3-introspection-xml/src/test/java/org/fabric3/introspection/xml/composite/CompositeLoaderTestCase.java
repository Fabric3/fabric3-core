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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.oasisopen.sca.Constants.SCA_NS;

import org.fabric3.model.type.PolicyAware;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 * @version $Rev: 7275 $ $Date: 2009-07-05 21:54:59 +0200 (Sun, 05 Jul 2009) $
 */
public class CompositeLoaderTestCase extends TestCase {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private CompositeLoader loader;
    private QName name;
    private IntrospectionContext introspectionContext;

    public void testLoadNameAndDefaultAutowire() throws Exception {
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getNamespaceContext()).andStubReturn(null);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        EasyMock.expect(reader.getAttributeValue(null, "targetNamespace")).andReturn(name.getNamespaceURI());
        EasyMock.expect(reader.getAttributeValue(null, "local")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "constrainingType")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn(null);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(COMPOSITE);
        EasyMock.replay(reader, introspectionContext);
        Composite type = loader.load(reader, introspectionContext);
        assertEquals(name, type.getName());
        assertEquals(Autowire.INHERITED, type.getAutowire());
        EasyMock.verify(reader, introspectionContext);
    }

    public void testAutowire() throws Exception {
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getNamespaceContext()).andStubReturn(null);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        EasyMock.expect(reader.getAttributeValue(null, "targetNamespace")).andReturn(name.getNamespaceURI());
        EasyMock.expect(reader.getAttributeValue(null, "local")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "constrainingType")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn("true");
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(COMPOSITE);
        EasyMock.replay(reader, introspectionContext);
        Composite type = loader.load(reader, introspectionContext);
        assertEquals(Autowire.ON, type.getAutowire());
        EasyMock.verify(reader, introspectionContext);
    }

    protected void setUp() throws Exception {
        super.setUp();
        introspectionContext = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(introspectionContext.getSourceBase()).andStubReturn(null);
        EasyMock.expect(introspectionContext.getClassLoader()).andStubReturn(null);
        EasyMock.expect(introspectionContext.getContributionUri()).andStubReturn(null);
        EasyMock.expect(introspectionContext.getTypeMapping(EasyMock.isA(Class.class))).andStubReturn(null);

        LoaderHelper loaderHelper = EasyMock.createMock(LoaderHelper.class);
        loaderHelper.loadPolicySetsAndIntents(EasyMock.isA(PolicyAware.class),
                                              EasyMock.isA(XMLStreamReader.class),
                                              EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(loaderHelper);

        LoaderRegistry registry = EasyMock.createNiceMock(LoaderRegistry.class);
        EasyMock.replay(registry);

        loader = new CompositeLoader(registry, null, null, null, loaderHelper);
        name = new QName("http://example.com", "composite");
    }
}
