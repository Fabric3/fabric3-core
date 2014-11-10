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
package org.fabric3.fabric.domain.collector;

import javax.xml.namespace.QName;

import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Marks and collects components, wires, and bindings during deployment and undeployment.
 */
public interface Collector {

    /**
     * Marks all components, wires, and bindings that are part of a context as provisioned.
     *
     * @param composite the root composite to traverse
     */
    void markAsProvisioned(LogicalCompositeComponent composite);

    /**
     * Mark components, bindings and wires belonging to the given deployable for collection.
     *
     * @param deployable the deployable being undeployed
     * @param composite  the composite containing components to be undeployed
     */
    void markForCollection(QName deployable, LogicalCompositeComponent composite);

    /**
     * Recursively collects marked components by removing them from the given composite.
     *
     * @param composite the composite to collect marked components from
     */
    void collect(LogicalCompositeComponent composite);

}
