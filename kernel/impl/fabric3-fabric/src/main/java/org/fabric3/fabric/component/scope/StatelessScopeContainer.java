/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.fabric.component.scope;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ExpirationPolicy;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitializationException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.objectfactory.ObjectCreationException;

/**
 * A scope context which manages stateless atomic component instances in a non-pooled fashion.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(ScopeContainer.class)
public class StatelessScopeContainer extends AbstractScopeContainer {

    public StatelessScopeContainer(@Monitor ScopeContainerMonitor monitor) {
        super(Scope.STATELESS, monitor);
    }

    @Override
    @Init
    public void start() {
        super.start();
    }

    @Destroy
    public void stop() {
        super.stop();
    }

    public InstanceWrapper getWrapper(AtomicComponent component, WorkContext workContext) throws InstanceLifecycleException {
        try {
            InstanceWrapper wrapper = component.createInstanceWrapper(workContext);
            wrapper.start(workContext);
            return wrapper;
        } catch (ObjectCreationException e) {
            throw new InstanceInitializationException("Error creating instance for: " + component.getUri(), e);
        }
    }

    public void returnWrapper(AtomicComponent component, WorkContext workContext, InstanceWrapper wrapper) throws InstanceDestructionException {
        wrapper.stop(workContext);
    }

    public void startContext(WorkContext workContext) throws GroupInitializationException {
        // do nothing
    }

    public void startContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        // do nothing
    }

    public void joinContext(WorkContext workContext) throws GroupInitializationException {
        // do nothing
    }

    public void joinContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        // do nothing
    }

    public void stopContext(WorkContext workContext) {
    }

    public void updated(AtomicComponent component, String referenceName) {
    }

    public void removed(AtomicComponent component, String referenceName) {

    }

    public void reinject() {
    }

}
