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
package org.fabric3.fabric.domain.executor;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.domain.command.StopContextCommand;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;

/**
 *
 */
public class StopContextCommandExecutorTestCase extends TestCase {

    public void testExecute() throws Exception {
        CommandExecutorRegistry executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        executorRegistry.register(EasyMock.eq(StopContextCommand.class), EasyMock.isA(StopContextCommandExecutor.class));

        ScopeContainer compositeContainer = EasyMock.createMock(ScopeContainer.class);
        compositeContainer.stopContext(EasyMock.isA(QName.class));
        ScopeContainer domainContainer = EasyMock.createMock(ScopeContainer.class);
        domainContainer.stopContext(EasyMock.isA(QName.class));
        ScopeRegistry scopeRegistry = EasyMock.createMock(ScopeRegistry.class);
        EasyMock.expect(scopeRegistry.getScopeContainer(Scope.COMPOSITE)).andReturn(compositeContainer);
        EasyMock.expect(scopeRegistry.getScopeContainer(Scope.DOMAIN)).andReturn(domainContainer);

        ChannelManager channelManager = EasyMock.createMock(ChannelManager.class);
        channelManager.stopContext(EasyMock.isA(QName.class));

        ContextMonitor monitor = EasyMock.createNiceMock(ContextMonitor.class);

        EasyMock.replay(executorRegistry, scopeRegistry, compositeContainer, channelManager, domainContainer, monitor);

        StopContextCommandExecutor executor = new StopContextCommandExecutor(executorRegistry, scopeRegistry, channelManager, monitor);
        executor.init();
        StopContextCommand command = new StopContextCommand(new QName("test", "component"), true);
        executor.execute(command);

        EasyMock.verify(executorRegistry, scopeRegistry, compositeContainer, channelManager, domainContainer, monitor);

    }

}
