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
package org.fabric3.contribution.processor;

import java.net.URI;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoader;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class DeploymentPlanXmlProcessorTestCase extends TestCase {

    public void testProcessContent() throws Exception {
        Contribution contribution = new Contribution(URI.create("contribution"));
        Resource resource = new Resource(contribution, null, null);
        contribution.addResource(resource);

        IntrospectionContext context = new DefaultIntrospectionContext();

        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);

        XmlProcessorRegistry registry = EasyMock.createNiceMock(XmlProcessorRegistry.class);
        XmlResourceElementLoader loader = EasyMock.createMock(XmlResourceElementLoader.class);
        loader.load(reader, resource, context);

        EasyMock.replay(registry, loader, reader);
        DeploymentPlanXmlProcessor processor = new DeploymentPlanXmlProcessor(registry, loader);

        processor.processContent(contribution, reader, context);

        EasyMock.verify(registry, loader, reader);
    }
}
