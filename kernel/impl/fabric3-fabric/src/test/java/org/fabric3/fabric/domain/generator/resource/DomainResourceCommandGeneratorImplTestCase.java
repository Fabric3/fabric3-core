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

import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.api.model.type.component.ResourceDefinition;
import org.fabric3.spi.domain.generator.resource.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalResourceDefinition;

/**
 *
 */
public class DomainResourceCommandGeneratorImplTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testIncrementalBuild() throws Exception {
        ResourceGenerator<MockDefinition> resourceGenerator = EasyMock.createMock(ResourceGenerator.class);
        EasyMock.expect(resourceGenerator.generateResource(EasyMock.isA(LogicalResource.class))).andReturn(new MockPhysicalDefinition());
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(registry.getResourceGenerator(EasyMock.eq(MockDefinition.class))).andReturn(resourceGenerator);
        EasyMock.replay(registry, resourceGenerator);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new MockDefinition(), null);

        assertNotNull(generator.generateBuild(resource, true));

        EasyMock.verify(registry, resourceGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testIncrementalNoBuild() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new MockDefinition(), null);
        resource.setState(LogicalState.PROVISIONED);

        assertNull(generator.generateBuild(resource, true));

        EasyMock.verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    public void testIFullBuild() throws Exception {
        ResourceGenerator<MockDefinition> resourceGenerator = EasyMock.createMock(ResourceGenerator.class);
        EasyMock.expect(resourceGenerator.generateResource(EasyMock.isA(LogicalResource.class))).andReturn(new MockPhysicalDefinition());
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(registry.getResourceGenerator(EasyMock.eq(MockDefinition.class))).andReturn(resourceGenerator);
        EasyMock.replay(registry, resourceGenerator);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new MockDefinition(), null);
        resource.setState(LogicalState.PROVISIONED);

        assertNotNull(generator.generateBuild(resource, false));

        EasyMock.verify(registry, resourceGenerator);

    }

    @SuppressWarnings({"unchecked"})
    public void testIncrementalDispose() throws Exception {
        ResourceGenerator<MockDefinition> resourceGenerator = EasyMock.createMock(ResourceGenerator.class);
        EasyMock.expect(resourceGenerator.generateResource(EasyMock.isA(LogicalResource.class))).andReturn(new MockPhysicalDefinition());
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(registry.getResourceGenerator(EasyMock.eq(MockDefinition.class))).andReturn(resourceGenerator);
        EasyMock.replay(registry, resourceGenerator);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new MockDefinition(), null);
        resource.setState(LogicalState.MARKED);
        assertNotNull(generator.generateDispose(resource, true));

        EasyMock.verify(registry, resourceGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testIncrementalNoDispose() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new MockDefinition(), null);
        resource.setState(LogicalState.PROVISIONED);

        assertNull(generator.generateDispose(resource, true));

        EasyMock.verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    public void testIFullDispose() throws Exception {
        ResourceGenerator<MockDefinition> resourceGenerator = EasyMock.createMock(ResourceGenerator.class);
        EasyMock.expect(resourceGenerator.generateResource(EasyMock.isA(LogicalResource.class))).andReturn(new MockPhysicalDefinition());
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(registry.getResourceGenerator(EasyMock.eq(MockDefinition.class))).andReturn(resourceGenerator);
        EasyMock.replay(registry, resourceGenerator);

        DomainResourceCommandGeneratorImpl generator = new DomainResourceCommandGeneratorImpl(registry);

        LogicalResource resource = new LogicalResource(new MockDefinition(), null);
        resource.setState(LogicalState.MARKED);

        assertNotNull(generator.generateDispose(resource, true));

        EasyMock.verify(registry, resourceGenerator);

    }

    private class MockDefinition extends ResourceDefinition {
        private static final long serialVersionUID = -4013178193696275298L;
    }

    private class MockPhysicalDefinition extends PhysicalResourceDefinition {
        private static final long serialVersionUID = -4013178193696275298L;
    }
}
