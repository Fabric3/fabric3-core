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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fabric3.api.model.type.resource.jndi.JndiContext;

/**
 * Builds {@link JndiContext}s.
 */
public class JndiContextBuilder extends AbstractBuilder {
    private Map<String, Properties> map;

    /**
     * Creates a builder.
     *
     * @return the builder
     */
    public static JndiContextBuilder newBuilder() {
        return new JndiContextBuilder();
    }

    private JndiContextBuilder() {
        map = new HashMap<>();
    }

    public JndiContextBuilder add(String name, Properties properties) {
        checkState();
        map.put(name, properties);
        return this;
    }

    public JndiContext build() {
        checkState();
        freeze();
        return new JndiContext(map);
    }
}
