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
package org.fabric3.api.model.type.component;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * A service.
 */
public class ServiceDefinition<P extends ModelObject> extends BindableDefinition<P> {
    private static final long serialVersionUID = -3331868180749278028L;

    private String name;
    private ServiceContract serviceContract;
    private List<BindingDefinition> callbackBindings = new ArrayList<>();


    public ServiceDefinition(String name) {
        this(name, null);
    }

    public ServiceDefinition(String name, ServiceContract serviceContract) {
        this.name = name;
        this.serviceContract = serviceContract;
        if (serviceContract != null) {
            serviceContract.setParent(this);
        }
    }

    /**
     * Return the name of this service definition.
     *
     * @return the name of this service definition
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the service contract
     *
     * @return the service contract
     */
    public ServiceContract getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the service contract
     *
     * @param contract the service contract
     */
    public void setServiceContract(ServiceContract contract) {
        this.serviceContract = contract;
    }

    /**
     * @return List of callback bindings defined against the reference.
     */
    public List<BindingDefinition> getCallbackBindings() {
        return callbackBindings;
    }

    /**
     * @param binding callback binding to be added.
     */
    public void addCallbackBinding(BindingDefinition binding) {
        this.callbackBindings.add(binding);
    }

}
