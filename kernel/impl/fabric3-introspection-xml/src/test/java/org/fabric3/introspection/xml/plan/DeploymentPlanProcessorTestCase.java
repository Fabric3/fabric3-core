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
package org.fabric3.introspection.xml.plan;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 *
 */
public class DeploymentPlanProcessorTestCase extends TestCase {

    private static final String XML =
            "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n" +
                    "<plan xmlns=\"urn:fabric3.org\" name=\"testPlan\">\n" +
                    "   <mappings>\n" +
                    "      <mapping deployable=\"deployable1\" zone=\"zone1\"/>  \n" +
                    "      <mapping deployable=\"deployable2\" zone=\"zone2\"/>  \n" +
                    "   </mappings>\n" +
                    "</plan>";


    private DeploymentPlanProcessor processor;
    private XMLStreamReader reader;

    public void testProcess() throws Exception {
        Resource resource = new Resource(null, null, "test");
        QName qName = new QName(DeploymentPlanConstants.PLAN_NAMESPACE, "testPlan");
        QNameSymbol symbol = new QNameSymbol(qName);
        ResourceElement<QNameSymbol, DeploymentPlan> element = new ResourceElement<>(symbol);
        resource.addResourceElement(element);
        IntrospectionContext context = new DefaultIntrospectionContext();
        processor.load(reader, resource, context);
        DeploymentPlan plan = element.getValue();
        assertNotNull(plan);
        assertEquals(2, plan.getDeployableMappings().size());
        assertEquals("zone1", plan.getDeployableMappings().get(new QName(null, "deployable1")));
        assertEquals("zone2", plan.getDeployableMappings().get(new QName(null, "deployable2")));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        XmlResourceElementLoaderRegistry registry = EasyMock.createNiceMock(XmlResourceElementLoaderRegistry.class);
        EasyMock.replay(registry);
        processor = new DeploymentPlanProcessor(registry);
        processor.init();
        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
    }
}