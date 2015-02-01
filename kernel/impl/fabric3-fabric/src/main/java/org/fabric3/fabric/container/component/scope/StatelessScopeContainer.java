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
package org.fabric3.fabric.container.component.scope;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.GroupInitializationException;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopedComponent;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Service;

/**
 * A scope context which manages stateless atomic component instances in a non-pooled fashion.
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

    public Object getInstance(ScopedComponent component) throws Fabric3Exception {
        Object instance = component.createInstance();
        component.startInstance(instance);
        return instance;
    }

    public void releaseInstance(ScopedComponent component, Object instance) throws Fabric3Exception {
        component.stopInstance(instance);
    }

    public List<Object> getActiveInstances(ScopedComponent component) {
        return Collections.emptyList();
    }

    public void startContext(QName deployable) throws GroupInitializationException {
        // no-op
    }

    public void stopContext(QName deployable) {
        // no-op
    }

    public void reinject() {
        // no-op
    }

}
