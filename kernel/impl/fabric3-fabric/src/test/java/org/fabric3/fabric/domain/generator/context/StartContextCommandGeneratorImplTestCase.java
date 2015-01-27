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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
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
    public void testStart() throws Exception {
        StartContextCommandGeneratorImpl generator = new StartContextCommandGeneratorImpl();

       List<CompensatableCommand> commands = generator.generate(createComponents());
        assertEquals(1, commands.size());
    }

    @SuppressWarnings({"unchecked"})
    public void testNoStart() throws Exception {
        StartContextCommandGeneratorImpl generator = new StartContextCommandGeneratorImpl();

        List<LogicalComponent<?>> components = new ArrayList<>();
        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), null, null);
        component1.setZone("zone1");
        component1.setDeployable(DEPLOYABLE1);
        component1.setState(LogicalState.PROVISIONED);
        components.add(component1);

       List<CompensatableCommand> commands = generator.generate(components);

        assertTrue(commands.isEmpty());
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
        component2.setZone("zone1");
        LogicalComponent<?> component3 = new LogicalComponent(URI.create("component3"), null, null);
        component3.setState(LogicalState.MARKED);
        component3.setDeployable(DEPLOYABLE3);
        component3.setZone("zone1");

        components.add(component1);
        components.add(component2);
        components.add(component3);
        return components;
    }


}
