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
package org.fabric3.fabric.domain.generator.component;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.fabric.container.command.StopComponentCommand;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 *
 */
public class StopComponentCommandGeneratorTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testIncrementalStop() throws Exception {
        StopComponentCommandGenerator generator = new StopComponentCommandGenerator();
        URI uri = URI.create("component");
        LogicalComponent<?> component = new LogicalComponent(uri, null, null);
        component.setState(LogicalState.MARKED);

        StopComponentCommand command = generator.generate(component, true);

        assertEquals(uri, command.getUri());
    }

    @SuppressWarnings({"unchecked"})
    public void testFullStop() throws Exception {
        StopComponentCommandGenerator generator = new StopComponentCommandGenerator();
        URI uri = URI.create("component");
        LogicalComponent<?> component = new LogicalComponent(uri, null, null);
        component.setState(LogicalState.MARKED);
        StopComponentCommand command = generator.generate(component, false);

        assertEquals(uri, command.getUri());
    }

    @SuppressWarnings({"unchecked"})
    public void testIncrementalNoStop() throws Exception {
        StopComponentCommandGenerator generator = new StopComponentCommandGenerator();
        URI uri = URI.create("component");
        LogicalComponent<?> component = new LogicalComponent(uri, null, null);
        component.setState(LogicalState.PROVISIONED);

        assertNull(generator.generate(component, true));
    }


}
