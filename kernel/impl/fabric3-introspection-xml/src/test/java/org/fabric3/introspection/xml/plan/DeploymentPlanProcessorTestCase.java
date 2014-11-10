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
import org.fabric3.spi.model.plan.DeploymentPlan;

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