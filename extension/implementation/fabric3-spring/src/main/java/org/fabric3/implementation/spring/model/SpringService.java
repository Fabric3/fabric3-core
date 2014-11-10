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
 */
package org.fabric3.implementation.spring.model;

import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * An SCA service definition in a Spring application context.
 */
public class SpringService extends ServiceDefinition {
    private static final long serialVersionUID = 204519855340684340L;
    private String target;

    /**
     * Constructor.
     *
     * @param name            the service name
     * @param serviceContract the service contract
     * @param target          the service target name
     */
    public SpringService(String name, ServiceContract serviceContract, String target) {
        super(name, serviceContract);
        this.target = target;
    }

    /**
     * Returns the configured service target name.
     *
     * @return the configured service target name
     */
    public String getTarget() {
        return target;
    }
}
