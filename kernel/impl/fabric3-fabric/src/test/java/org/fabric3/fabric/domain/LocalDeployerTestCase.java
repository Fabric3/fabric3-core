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
package org.fabric3.fabric.domain;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.domain.DeployerMonitor;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.container.executor.ExecutionException;
import org.fabric3.spi.domain.generator.Deployment;

import static org.fabric3.api.host.Names.LOCAL_ZONE;

/**
 *
 */
public class LocalDeployerTestCase extends TestCase {

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testRollback() throws Exception {
        ScopeContainer scopeContainer = EasyMock.createMock(ScopeContainer.class);
        scopeContainer.reinject();

        ScopeRegistry scopeRegistry = EasyMock.createMock(ScopeRegistry.class);
        EasyMock.expect(scopeRegistry.getScopeContainer(Scope.COMPOSITE)).andReturn(scopeContainer);

        DeployerMonitor monitor = EasyMock.createMock(DeployerMonitor.class);
        monitor.rollback("local");

        CompensatableCommand rollbackCommand = EasyMock.createMock(CompensatableCommand.class);
        CompensatableCommand command1 = EasyMock.createMock(CompensatableCommand.class);
        EasyMock.expect(command1.getCompensatingCommand()).andReturn(rollbackCommand);

        CompensatableCommand command2 = EasyMock.createMock(CompensatableCommand.class);


        CommandExecutorRegistry executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        executorRegistry.execute(command1);
        executorRegistry.execute(command2);
        EasyMock.expectLastCall().andThrow(new ExecutionException("test error"));
        executorRegistry.execute(rollbackCommand);

        EasyMock.replay(executorRegistry, scopeRegistry, scopeContainer, monitor, command1, command2, rollbackCommand);

        LocalDeployer deployer = new LocalDeployer(executorRegistry, scopeRegistry, monitor);

        Deployment currentDeployment = new Deployment("current");
        currentDeployment.addCommand(LOCAL_ZONE, command1);
        currentDeployment.addCommand(LOCAL_ZONE, command2);

        Deployment fullDeployment = new Deployment("full");
        DeploymentPackage deploymentPackage = new DeploymentPackage(currentDeployment, fullDeployment);

        try {
            deployer.deploy(deploymentPackage);
            fail();
        } catch (DeploymentException e) {
            // expected 
            assertTrue(e.getCause() instanceof ExecutionException);
        }

        EasyMock.verify(executorRegistry, scopeContainer, scopeRegistry, monitor, command1, command2, rollbackCommand);
    }
}
