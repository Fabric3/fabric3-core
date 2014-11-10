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
import java.util.List;

/**
 * A producer configured on a component.
 */
public class ComponentProducer extends AbstractProducer<ComponentDefinition> {
    private static final long serialVersionUID = -4230400252060306972L;

    /**
     * Constructor.
     *
     * @param name    the name of the producer being configured
     * @param targets the channel targets
     */
    public ComponentProducer(String name, List<URI> targets) {
        super(name);
        this.targets = targets;
    }

    /**
     * Constructor.
     *
     * @param name the name of the producer being configured
     */
    public ComponentProducer(String name) {
        super(name);
    }


}