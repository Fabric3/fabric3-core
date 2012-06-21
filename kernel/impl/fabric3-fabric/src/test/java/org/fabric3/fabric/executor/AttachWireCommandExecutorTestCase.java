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
package org.fabric3.fabric.executor;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.builder.Connector;
import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * @version $Rev: 10102 $ $Date: 2011-03-15 23:59:22 -0700 (Tue, 15 Mar 2011) $
 */
public class AttachWireCommandExecutorTestCase extends TestCase {

    public void testAttachExecute() throws Exception {
        CommandExecutorRegistry executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        Connector connector = EasyMock.createMock(Connector.class);
        executorRegistry.register(EasyMock.eq(AttachWireCommand.class), EasyMock.isA(AttachWireCommandExecutor.class));
        connector.connect(EasyMock.isA(PhysicalWireDefinition.class));
        EasyMock.replay(executorRegistry, connector);

        AttachWireCommandExecutor executor = new AttachWireCommandExecutor(executorRegistry, connector);
        executor.init();
        PhysicalWireDefinition definition = new PhysicalWireDefinition(null, null, null);
        AttachWireCommand command = new AttachWireCommand();
        command.setPhysicalWireDefinition(definition);
        executor.execute(command);
        EasyMock.verify(executorRegistry, connector);

    }

}
