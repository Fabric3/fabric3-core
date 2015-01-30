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
 * A reference.
 */
public class ReferenceDefinition<P extends ModelObject> extends AbstractReference<P> {
    private static final long serialVersionUID = 4641581818938572132L;

    /**
     * Constructor.
     *
     * @param name            the reference name
     * @param serviceContract the service contract required by this reference
     */
    public ReferenceDefinition(String name, ServiceContract serviceContract) {
        this(name, serviceContract, Multiplicity.ONE_ONE);
    }

    /**
     * Constructor.
     *
     * @param name         the reference name
     * @param multiplicity the reference multiplicity
     */
    public ReferenceDefinition(String name, Multiplicity multiplicity) {
        this(name, null, multiplicity);
    }

    /**
     * Constructor.
     *
     * @param name the reference name
     */
    public ReferenceDefinition(String name) {
        this(name, null, Multiplicity.ONE_ONE);
    }

    /**
     * Constructor.
     *
     * @param name            the reference name
     * @param serviceContract the service contract required by this reference
     * @param multiplicity    the reference multiplicity
     */
    public ReferenceDefinition(String name, ServiceContract serviceContract, Multiplicity multiplicity) {
        super(name, serviceContract, multiplicity);
    }

}
