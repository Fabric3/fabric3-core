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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.domain.generator.context;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.fabric.domain.command.StopContextCommand;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 *
 */
public class StopContextCommandGeneratorImplTestCase extends TestCase {
    private static final QName DEPLOYABLE1 = new QName("component", "1");
    private static final QName DEPLOYABLE2 = new QName("component", "2");
    private static final QName DEPLOYABLE3 = new QName("component", "3");

    @SuppressWarnings({"unchecked"})
    public void testStop() throws Exception {
        StopContextCommandGeneratorImpl generator = new StopContextCommandGeneratorImpl();

        Map<String, List<CompensatableCommand>> commands = generator.generate(createComponents());
        assertEquals(1, commands.size());
        List<CompensatableCommand> zone1 = commands.get("zone1");
        assertEquals(1, zone1.size());
        StopContextCommand command = (StopContextCommand) zone1.get(0);
        assertEquals(DEPLOYABLE1, command.getDeployable());
    }

    @SuppressWarnings({"unchecked"})
    public void testNoStop() throws Exception {
        StopContextCommandGeneratorImpl generator = new StopContextCommandGeneratorImpl();

        List<LogicalComponent<?>> components = new ArrayList<>();
        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), null, null);
        component1.setZone("zone1");
        component1.setDeployable(DEPLOYABLE1);
        component1.setState(LogicalState.PROVISIONED);
        components.add(component1);

        Map<String, List<CompensatableCommand>> commands = generator.generate(components);

        assertTrue(commands.isEmpty());
    }

    @SuppressWarnings({"unchecked"})
    public void testTwoZoneStop() throws Exception {
        StopContextCommandGeneratorImpl generator = new StopContextCommandGeneratorImpl();

        List<LogicalComponent<?>> components = new ArrayList<>();

        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), null, null);
        component1.setZone("zone1");
        component1.setDeployable(DEPLOYABLE1);
        component1.setState(LogicalState.MARKED);
        components.add(component1);

        LogicalComponent<?> component2 = new LogicalComponent(URI.create("component2"), null, null);
        component2.setZone("zone2");
        component2.setDeployable(DEPLOYABLE2);
        component2.setState(LogicalState.MARKED);
        components.add(component2);

        Map<String, List<CompensatableCommand>> commands = generator.generate(components);

        assertEquals(2, commands.size());
    }

    @SuppressWarnings({"unchecked"})
    private List<LogicalComponent<?>> createComponents() {
        List<LogicalComponent<?>> components = new ArrayList<>();

        LogicalComponent<?> component1 = new LogicalComponent(URI.create("component1"), null, null);
        component1.setZone("zone1");
        component1.setDeployable(DEPLOYABLE1);
        component1.setState(LogicalState.MARKED);
        LogicalComponent<?> component2 = new LogicalComponent(URI.create("component2"), null, null);
        component2.setState(LogicalState.PROVISIONED);
        component2.setDeployable(DEPLOYABLE2);
        component2.setZone("zone3");
        LogicalComponent<?> component3 = new LogicalComponent(URI.create("component3"), null, null);
        component3.setDeployable(DEPLOYABLE3);
        component3.setZone("zone2");

        components.add(component1);
        components.add(component2);
        components.add(component3);
        return components;
    }


}
