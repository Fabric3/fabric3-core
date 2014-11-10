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
import java.util.Map;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.fabric.container.command.StartContextCommand;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 *
 */
public class StartContextCommandGeneratorImplTestCase extends TestCase {
    private static final QName DEPLOYABLE1 = new QName("component", "1");
    private static final QName DEPLOYABLE2 = new QName("component", "2");
    private static final QName DEPLOYABLE3 = new QName("component", "3");

    @SuppressWarnings({"unchecked"})
    public void testIncrementalStart() throws Exception {
        StartContextCommandGeneratorImpl generator = new StartContextCommandGeneratorImpl();

        Map<String, List<CompensatableCommand>> commands = generator.generate(createComponents(), true);
        assertEquals(1, commands.size());
        List<CompensatableCommand> zone1 = commands.get("zone1");
        assertEquals(1, zone1.size());
        StartContextCommand command = (StartContextCommand) zone1.get(0);
        assertEquals(DEPLOYABLE1, command.getDeployable());
    }

    @SuppressWarnings({"unchecked"})
    public void testFullStart() throws Exception {
        StartContextCommandGeneratorImpl generator = new StartContextCommandGeneratorImpl();

        Map<String, List<CompensatableCommand>> commands = generator.generate(createComponents(), false);
        assertEquals(3, commands.size());
        List<CompensatableCommand> zone1 = commands.get("zone1");
        assertEquals(1, zone1.size());
        for (CompensatableCommand entry : zone1) {
            StartContextCommand command = (StartContextCommand) entry;
            assertTrue(DEPLOYABLE1.equals(command.getDeployable()) || DEPLOYABLE2.equals(command.getDeployable()));
        }
    }

    @SuppressWarnings({"unchecked"})
    public void testIncrementalNoStart() throws Exception {
        StartContextCommandGeneratorImpl generator = new StartContextCommandGeneratorImpl();

        List<LogicalComponent<?>> components = new ArrayList<>();
        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), null, null);
        component1.setZone("zone1");
        component1.setDeployable(DEPLOYABLE1);
        component1.setState(LogicalState.PROVISIONED);
        components.add(component1);

        Map<String, List<CompensatableCommand>> commands = generator.generate(components, true);

        assertTrue(commands.isEmpty());
    }

    @SuppressWarnings({"unchecked"})
    public void testTwoZoneIncrementalStart() throws Exception {
        StartContextCommandGeneratorImpl generator = new StartContextCommandGeneratorImpl();

        List<LogicalComponent<?>> components = new ArrayList<>();

        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), null, null);
        component1.setZone("zone1");
        component1.setDeployable(DEPLOYABLE1);
        components.add(component1);

        LogicalComponent<?> component2 = new LogicalComponent(URI.create("component2"), null, null);
        component2.setZone("zone2");
        component2.setDeployable(DEPLOYABLE2);
        components.add(component2);

        Map<String, List<CompensatableCommand>> commands = generator.generate(components, true);

        assertEquals(2, commands.size());
    }

    @SuppressWarnings({"unchecked"})
    private List<LogicalComponent<?>> createComponents() {
        List<LogicalComponent<?>> components = new ArrayList<>();

        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), null, null);
        component1.setZone("zone1");
        component1.setDeployable(DEPLOYABLE1);
        LogicalComponent<?> component2 = new LogicalComponent(URI.create("component2"), null, null);
        component2.setState(LogicalState.PROVISIONED);
        component2.setDeployable(DEPLOYABLE2);
        component2.setZone("zone3");
        LogicalComponent<?> component3 = new LogicalComponent(URI.create("component3"), null, null);
        component3.setState(LogicalState.MARKED);
        component3.setDeployable(DEPLOYABLE3);
        component3.setZone("zone2");

        components.add(component1);
        components.add(component2);
        components.add(component3);
        return components;
    }


}
