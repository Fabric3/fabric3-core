/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.fabric.generator.component;

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.command.BuildComponentCommand;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * @version $Rev$ $Date$
 */
public class BuildComponentCommandGeneratorTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testIncrementalBuild() throws Exception {
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        EasyMock.expect(componentGenerator.generate(EasyMock.isA(LogicalComponent.class))).andReturn(new MockDefinition());
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(registry.getComponentGenerator(EasyMock.eq(MockImplementation.class))).andReturn(componentGenerator);
        EasyMock.replay(registry, componentGenerator);

        BuildComponentCommandGenerator generator = new BuildComponentCommandGenerator(registry, 0);

        ComponentDefinition<MockImplementation> definition = new ComponentDefinition<MockImplementation>("component", new MockImplementation());
        LogicalComponent<MockImplementation> component = new LogicalComponent<MockImplementation>(URI.create("component"), definition, null);

        BuildComponentCommand command = generator.generate(component, true);
        assertNotNull(command.getDefinition());
        EasyMock.verify(registry, componentGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testIncrementalNoBuild() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        BuildComponentCommandGenerator generator = new BuildComponentCommandGenerator(registry, 0);

        ComponentDefinition<MockImplementation> definition = new ComponentDefinition<MockImplementation>("component", new MockImplementation());
        LogicalComponent<MockImplementation> component = new LogicalComponent<MockImplementation>(URI.create("component"), definition, null);
        component.setState(LogicalState.PROVISIONED);

        assertNull(generator.generate(component, true));
        EasyMock.verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    public void testIFullBuild() throws Exception {
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        EasyMock.expect(componentGenerator.generate(EasyMock.isA(LogicalComponent.class))).andReturn(new MockDefinition());
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(registry.getComponentGenerator(EasyMock.eq(MockImplementation.class))).andReturn(componentGenerator);
        EasyMock.replay(registry, componentGenerator);

        BuildComponentCommandGenerator generator = new BuildComponentCommandGenerator(registry, 0);

        ComponentDefinition<MockImplementation> definition = new ComponentDefinition<MockImplementation>("component", new MockImplementation());
        LogicalComponent<MockImplementation> component = new LogicalComponent<MockImplementation>(URI.create("component"), definition, null);

        BuildComponentCommand command = generator.generate(component, false);
        assertNotNull(command.getDefinition());
        EasyMock.verify(registry, componentGenerator);
    }

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = -4177749325047896524L;

        @Override
        public QName getType() {
            return null;
        }
    }

    private class MockDefinition extends PhysicalComponentDefinition {
        private static final long serialVersionUID = 1097054400657294542L;
    }
}
