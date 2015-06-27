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
package org.fabric3.fabric.domain.generator.context;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.fabric.container.command.Command;
import org.fabric3.fabric.container.command.StopContextCommand;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 *
 */
public class StopContextCommandGeneratorImplTestCase extends TestCase {
    private static final URI CONTRIBUTION_1 = URI.create("1");
    private static final URI CONTRIBUTION_2 = URI.create("2");
    private static final URI CONTRIBUTION_3 = URI.create("3");

    @SuppressWarnings({"unchecked"})
    public void testStop() throws Exception {
        StopContextCommandGeneratorImpl generator = new StopContextCommandGeneratorImpl();

        List<Command> commands = generator.generate(createComponents());
        assertEquals(1, commands.size());
        StopContextCommand command = (StopContextCommand) commands.get(0);
        assertEquals(CONTRIBUTION_1, command.getUri());
    }

    @SuppressWarnings({"unchecked"})
    public void testNoStop() throws Exception {
        StopContextCommandGeneratorImpl generator = new StopContextCommandGeneratorImpl();

        List<LogicalComponent<?>> components = new ArrayList<>();
        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), new Component("component1"), null);
        component1.getDefinition().setContributionUri(CONTRIBUTION_1);
        component1.setState(LogicalState.PROVISIONED);
        components.add(component1);

        List<Command> commands = generator.generate(components);

        assertTrue(commands.isEmpty());
    }

    @SuppressWarnings({"unchecked"})
    public void testTwoZoneStop() throws Exception {
        StopContextCommandGeneratorImpl generator = new StopContextCommandGeneratorImpl();

        List<LogicalComponent<?>> components = new ArrayList<>();

        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), new Component("component1"), null);
        component1.getDefinition().setContributionUri(CONTRIBUTION_1);
        component1.setState(LogicalState.MARKED);
        components.add(component1);

        LogicalComponent<?> component2 = new LogicalComponent(URI.create("component2"), new Component("component2"), null);
        component2.getDefinition().setContributionUri(CONTRIBUTION_2);
        component2.setState(LogicalState.MARKED);
        components.add(component2);

        List<Command> commands = generator.generate(components);

        assertEquals(2, commands.size());
    }

    @SuppressWarnings({"unchecked"})
    private List<LogicalComponent<?>> createComponents() {
        List<LogicalComponent<?>> components = new ArrayList<>();

        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), new Component("component1"), null);
        component1.getDefinition().setContributionUri(CONTRIBUTION_1);
        component1.setState(LogicalState.MARKED);
        LogicalComponent<?> component2 = new LogicalComponent(URI.create("component2"), new Component("component2"), null);
        component2.setState(LogicalState.PROVISIONED);
        component2.getDefinition().setContributionUri(CONTRIBUTION_2);
        LogicalComponent<?> component3 = new LogicalComponent(URI.create("component3"), new Component("component3"), null);
        component3.getDefinition().setContributionUri(CONTRIBUTION_3);

        components.add(component1);
        components.add(component2);
        components.add(component3);
        return components;
    }

}
