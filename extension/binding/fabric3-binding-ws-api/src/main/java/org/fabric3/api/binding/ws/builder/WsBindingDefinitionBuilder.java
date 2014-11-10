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
package org.fabric3.api.binding.ws.builder;

import java.net.URI;
import java.util.Map;

import org.fabric3.api.binding.ws.model.WsBindingDefinition;
import org.fabric3.api.model.type.builder.AbstractBuilder;

/**
 * Builder for the WS binding.
 */
public class WsBindingDefinitionBuilder extends AbstractBuilder {
    private WsBindingDefinition binding;

    public static WsBindingDefinitionBuilder newBuilder() {
        return new WsBindingDefinitionBuilder();
    }

    public WsBindingDefinitionBuilder() {
        this("ws.binding");
    }

    public WsBindingDefinitionBuilder(String name) {
        this.binding = new WsBindingDefinition();
        binding.setName(name);
    }

    public WsBindingDefinitionBuilder configuration(Map<String, String> configuration) {
        checkState();
        binding.setConfiguration(configuration);
        return this;
    }

    public WsBindingDefinitionBuilder uri(URI uri) {
        checkState();
        binding.setTargetUri(uri);
        return this;
    }

}
