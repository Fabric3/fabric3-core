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
package org.fabric3.resource.model;

import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * A resource sourced from the runtime domain.
 */
public class SystemSourcedResourceReference extends ResourceReference {
    private static final long serialVersionUID = 8542386357450347005L;
    private String mappedName;

    public SystemSourcedResourceReference(String name, boolean optional, String mappedName, ServiceContract serviceContract) {
        super(name, serviceContract, optional);
        this.mappedName = mappedName;
    }

    public String getMappedName() {
        return this.mappedName;
    }

}
