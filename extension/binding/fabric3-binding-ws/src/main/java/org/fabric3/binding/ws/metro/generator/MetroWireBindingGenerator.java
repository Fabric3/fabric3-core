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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.ws.metro.generator;

import java.util.List;
import java.util.Map;

import org.fabric3.api.binding.ws.model.WsBinding;
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.binding.ws.metro.provision.MetroWireSourceDefinition;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates PhysicalWireSourceDefinitions and PhysicalWireTargetDefinitions for the Metro web services binding.
 */
public class MetroWireBindingGenerator implements WireBindingGenerator<WsBinding> {
    private Map<Class<?>, MetroGeneratorDelegate> delegates;

    @Reference
    public void setDelegates(Map<Class<?>, MetroGeneratorDelegate> delegates) {
        this.delegates = delegates;
    }

    @SuppressWarnings({"unchecked"})
    public MetroWireSourceDefinition generateSource(LogicalBinding<WsBinding> binding, ServiceContract contract, List<LogicalOperation> operations) throws ContainerException {
        MetroGeneratorDelegate delegate = getDelegate(contract);
        return delegate.generateSource(binding, contract);
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalWireTargetDefinition generateTarget(LogicalBinding<WsBinding> binding, ServiceContract contract, List<LogicalOperation> operations) throws ContainerException {
        MetroGeneratorDelegate delegate = getDelegate(contract);
        return delegate.generateTarget(binding, contract);
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalWireTargetDefinition generateServiceBindingTarget(LogicalBinding<WsBinding> serviceBinding,
                                                                     ServiceContract contract,
                                                                     List<LogicalOperation> operations) throws ContainerException {
        MetroGeneratorDelegate delegate = getDelegate(contract);
        return delegate.generateServiceBindingTarget(serviceBinding, contract);
    }


    private MetroGeneratorDelegate getDelegate(ServiceContract contract) throws ContainerException {
        MetroGeneratorDelegate<?> delegate = delegates.get(contract.getClass());
        if (delegate == null) {
            throw new ContainerException("Generator delegate not found for type: " + contract.getClass().getName());
        }
        return delegate;
    }

}