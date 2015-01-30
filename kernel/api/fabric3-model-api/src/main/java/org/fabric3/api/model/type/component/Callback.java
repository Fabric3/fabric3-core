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

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * A component type callback.
 */
public class Callback extends ModelObject<ComponentType> {
    private static final long serialVersionUID = -1845071329121684755L;

    private String name;
    private ServiceContract serviceContract;

    public Callback(String name, ServiceContract serviceContract) {
        this.name = name;
        this.serviceContract = serviceContract;
        if (serviceContract != null) {
            serviceContract.setParent(this);
        }
    }

    /**
     * The name of the callback
     *
     * @return the name of the callback
     */
    public String getName() {
        return name;
    }

    /**
     * Returned the service contract for the callback.
     *
     * @return the service contract
     */
    public ServiceContract getServiceContract() {
        return serviceContract;
    }

}