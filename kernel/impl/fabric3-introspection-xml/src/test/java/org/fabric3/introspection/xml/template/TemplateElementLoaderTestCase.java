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
 *
 */
package org.fabric3.introspection.xml.template;

import java.io.ByteArrayInputStream;
import java.net.URI;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.introspection.xml.MockXMLFactory;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TemplateRegistry;

/**
 *
 */
@EagerInit
public class TemplateElementLoaderTestCase extends TestCase {
    private static final URI CONTRIBUTION_URI = URI.create("test");

    private static final String XML = "<template name='template'><binding/></template>";
    private static final String XML_NO_BODY = "<template name='template'></template>";
    private static final String XML_NO_NAME = "<binding.template/>";

    private TemplateRegistry templateRegistry;
    private TemplateElementLoader loader;
    private IntrospectionContext context;
    private MockXMLFactory factory;
    private LoaderRegistry loaderRegistry;


    @SuppressWarnings({"serial"})
    public void testLoad() throws Exception {
        XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        ModelObject modelObject = new ModelObject() {
        };
        EasyMock.expect(loaderRegistry.load(reader, ModelObject.class, context)).andReturn(modelObject);
        templateRegistry.register("template", CONTRIBUTION_URI, modelObject);

        EasyMock.replay(templateRegistry, loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(templateRegistry, loaderRegistry);
        assertFalse(context.hasErrors());
    }

    public void testNoName() throws Exception {
        XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML_NO_NAME.getBytes()));
        reader.nextTag();

        EasyMock.replay(templateRegistry, loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(templateRegistry, loaderRegistry);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);
    }

    public void testNoBody() throws Exception {
        XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML_NO_BODY.getBytes()));
        reader.nextTag();

        EasyMock.replay(templateRegistry, loaderRegistry);

        loader.load(reader, context);
        EasyMock.verify(templateRegistry, loaderRegistry);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidTemplateDefinition);
    }

    protected void setUp() throws Exception {
        super.setUp();
        templateRegistry = EasyMock.createMock(TemplateRegistry.class);
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);

        loader = new TemplateElementLoader(loaderRegistry, templateRegistry);
        factory = new MockXMLFactory();
        context = new DefaultIntrospectionContext(CONTRIBUTION_URI, null, null, null);
    }

}
