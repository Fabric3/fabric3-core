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
*/
package org.fabric3.fabric.executor;

import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.fabric.command.StopContextCommand;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Stops a component context on a runtime.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class StopContextCommandExecutor implements CommandExecutor<StopContextCommand> {
    private CommandExecutorRegistry executorRegistry;
    private ScopeContainer compositeScopeContainer;
    private ScopeContainer domainScopeContainer;
    private ContextMonitor monitor;

    public StopContextCommandExecutor(@Reference CommandExecutorRegistry executorRegistry,
                                      @Reference ScopeRegistry scopeRegistry,
                                      @Monitor ContextMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.compositeScopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
        this.domainScopeContainer = scopeRegistry.getScopeContainer(Scope.DOMAIN);
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(StopContextCommand.class, this);
    }

    public void execute(StopContextCommand command) throws ExecutionException {
        QName deployable = command.getDeployable();
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(deployable);
        workContext.addCallFrame(frame);
        try {
            compositeScopeContainer.stopContext(workContext);
            if (domainScopeContainer != null) {
                // domain scope not available during bootstrap
                domainScopeContainer.stopContext(workContext);
            }
            if (monitor != null && command.isLog()) {
                monitor.undeployed(deployable);
            }
        } catch (ComponentException e) {
            throw new ExecutionException(e);
        }
    }

}

