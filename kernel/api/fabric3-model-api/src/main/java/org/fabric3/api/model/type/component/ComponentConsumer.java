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

/**
 * A consumer configured on a component.
 */
public class ComponentConsumer extends AbstractConsumer<ComponentDefinition> {
    private static final long serialVersionUID = -4230400252060306972L;

    /**
     * Constructor.
     *
     * @param name    the name of the consumer being configured
     * @param sources the channel targets
     */
    public ComponentConsumer(String name, List<URI> sources) {
        super(name);
        this.sources = sources;
        bindings = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param name the name of the consumer being configured
     */
    public ComponentConsumer(String name) {
        super(name);
        bindings = new ArrayList<>();
    }

}