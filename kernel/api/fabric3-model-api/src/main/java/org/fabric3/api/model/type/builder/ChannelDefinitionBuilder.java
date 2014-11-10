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
package org.fabric3.api.model.type.builder;

import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ChannelDefinition;

/**
 *
 */
public class ChannelDefinitionBuilder extends AbstractBuilder {

    protected final ChannelDefinition definition;

    /**
     * Creates a builder.
     *
     * @param name the channel name
     * @return the builder
     */
    public static ChannelDefinitionBuilder newBuilder(String name) {
        return new ChannelDefinitionBuilder(name);
    }

    public ChannelDefinitionBuilder type(String type) {
        checkState();
        definition.setType(type);
        return this;
    }

    /**
     * Sets the channel locality.
     *
     * @param local tru of the channel is local
     * @return the builder
     */
    public ChannelDefinitionBuilder local(boolean local) {
        checkState();
        definition.setLocal(local);
        return this;
    }

    /**
     * Adds a binding to the channel.
     *
     * @param binding the binding
     * @return the builder
     */
    public ChannelDefinitionBuilder binding(BindingDefinition binding) {
        checkState();
        definition.addBinding(binding);
        return this;
    }

    /**
     * Builds the channel definition.
     *
     * @return the definition
     */
    public ChannelDefinition build() {
        checkState();
        freeze();
        return definition;
    }

    protected ChannelDefinitionBuilder(String name) {
        definition = new ChannelDefinition(name);
    }

}
