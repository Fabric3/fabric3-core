/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.executor;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.command.BuildComponentCommand;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderListener;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * @version $Rev$ $Date$
 */
public class BuildComponentCommandExecutorTestCase extends TestCase {


    @SuppressWarnings({"unchecked"})
    public void testRegisterAndNotifyListener() throws Exception {
        PhysicalComponentDefinition definition = new MockDefinition();

        Component component = EasyMock.createMock(Component.class);
        component.setClassLoaderId(null);

        ComponentBuilder builder = EasyMock.createMock(ComponentBuilder.class);
        EasyMock.expect(builder.build(EasyMock.isA(PhysicalComponentDefinition.class))).andReturn(component);

        ComponentManager componentManager = EasyMock.createMock(ComponentManager.class);
        componentManager.register(component);

        ComponentBuilderListener listener = EasyMock.createMock(ComponentBuilderListener.class);
        listener.onBuild(EasyMock.isA(Component.class), EasyMock.isA(PhysicalComponentDefinition.class));
        EasyMock.replay(componentManager, builder, component, listener);

        BuildComponentCommandExecutor executor = new BuildComponentCommandExecutor(componentManager);
        Map<Class<?>, ComponentBuilder> map = Collections.<Class<?>, ComponentBuilder>singletonMap(MockDefinition.class, builder);
        executor.setBuilders(map);

        executor.setListeners(Collections.singletonList(listener));

        BuildComponentCommand command = new BuildComponentCommand(definition);
        executor.execute(command);

        EasyMock.verify(componentManager, builder, component, listener);
    }

    private class MockDefinition extends PhysicalComponentDefinition {
        private static final long serialVersionUID = -809769047230911419L;

        private MockDefinition() {
            setComponentUri(URI.create("test"));
        }
    }
}
