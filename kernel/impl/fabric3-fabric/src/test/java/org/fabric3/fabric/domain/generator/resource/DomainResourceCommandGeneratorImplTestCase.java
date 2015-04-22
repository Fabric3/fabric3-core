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
package org.fabric3.fabric.domain.generator.resource;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Resource;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.domain.generator.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalResource;

/**
 *
 */
public class DomainResourceCommandGeneratorImplTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testBuild() throws Exception {
        ResourceGenerator<Mock> resourceGenerator = EasyMock.createMock(ResourceGenerator.class);
        EasyMock.expect(resourceGenerator.generateResource(EasyMock.isA(LogicalResource.class))).andReturn(new MockResource());
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(registry.getResourceGenerator(EasyMock.eq(Mock.class))).andReturn(resourceGenerator);
        EasyMock.replay(registry, resourceGenerator);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new Mock(), null);

        assertTrue(generator.generateBuild(resource).isPresent());

        EasyMock.verify(registry, resourceGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testNoBuild() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new Mock(), null);
        resource.setState(LogicalState.PROVISIONED);

        assertFalse(generator.generateBuild(resource).isPresent());

        EasyMock.verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    public void testDispose() throws Exception {
        ResourceGenerator<Mock> resourceGenerator = EasyMock.createMock(ResourceGenerator.class);
        EasyMock.expect(resourceGenerator.generateResource(EasyMock.isA(LogicalResource.class))).andReturn(new MockResource());
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(registry.getResourceGenerator(EasyMock.eq(Mock.class))).andReturn(resourceGenerator);
        EasyMock.replay(registry, resourceGenerator);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new Mock(), null);
        resource.setState(LogicalState.MARKED);
        assertNotNull(generator.generateDispose(resource));

        EasyMock.verify(registry, resourceGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testNoDispose() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new Mock(), null);
        resource.setState(LogicalState.PROVISIONED);

        assertFalse(generator.generateDispose(resource).isPresent());

        EasyMock.verify(registry);
    }

    private class Mock extends Resource {
    }

    private class MockResource extends PhysicalResource {
    }
}
