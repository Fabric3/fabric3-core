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
 */
package org.fabric3.fabric.domain.instantiator.component;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.fabric.domain.instantiator.AutowireNormalizer;
import org.fabric3.api.model.type.component.Autowire;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 *
 */
public class AutowireNormalizerImplTestCase extends TestCase {
    AutowireNormalizer normalizer = new AutowireNormalizerImpl();

    @SuppressWarnings({"unchecked"})
    public void testInherit() throws Exception {
        LogicalComponent<?> component = setupComponent(Autowire.INHERITED);
        normalizer.normalize(component);
        assertEquals(Autowire.ON, component.getAutowire());
    }

    public void testNoInherit() throws Exception {
        LogicalComponent<?> component = setupComponent(Autowire.OFF);
        normalizer.normalize(component);
        assertEquals(Autowire.OFF, component.getAutowire());
    }

    @SuppressWarnings({"unchecked"})
    private LogicalComponent<?> setupComponent(Autowire autowire) {
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("parent"), null, null);
        parent.setAutowire(Autowire.ON);
        ComponentDefinition<?> definition = new ComponentDefinition("component");
        definition.setAutowire(autowire);
        return new LogicalComponent(URI.create("component"), definition, parent);
    }

}
