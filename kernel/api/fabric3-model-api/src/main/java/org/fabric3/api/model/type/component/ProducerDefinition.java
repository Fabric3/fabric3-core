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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * A producer introspected from a component type.
 */
public class ProducerDefinition<P extends ModelObject> extends BindableDefinition<P> {
    private static final long serialVersionUID = -4222312633353056234L;

    private String name;
    private ServiceContract serviceContract;

    protected List<URI> targets = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param name            the producer name
     * @param serviceContract the service contract required by this producer
     */
    public ProducerDefinition(String name, ServiceContract serviceContract) {
        this.name = name;
        this.serviceContract = serviceContract;
        if (serviceContract != null) {
            serviceContract.setParent(this);
        }

    }

    public ProducerDefinition(String name) {
        this.name = name;
    }

    /**
     * Returns the producer name.
     *
     * @return the producer name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the service contract required by this producer.
     *
     * @return the service contract required by this producer
     */
    public ServiceContract getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the service contract required by this producer.
     *
     * @param serviceContract the service contract required by this producer
     */
    public void setServiceContract(ServiceContract serviceContract) {
        this.serviceContract = serviceContract;
    }

    /**
     * Returns the URIs of channels this producer sends messages to.
     *
     * @return the URIs of channels this producer sends messages to
     */
    public List<URI> getTargets() {
        return targets;
    }

    /**
     * Sets the URIs of channels this producer sends messages to.
     *
     * @param targets the URIs of channels this producer sends messages to
     */
    public void setTargets(List<URI> targets) {
        this.targets = targets;
    }

    /**
     * Adds the URI of a channel this producer sends messages to.
     *
     * @param target the channel URI
     */
    public void addTarget(URI target) {
        targets.add(target);
    }

}