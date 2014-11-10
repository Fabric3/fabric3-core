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
package org.fabric3.api.model.type.resource.jndi;

import java.util.Map;
import java.util.Properties;

import org.fabric3.api.model.type.component.ResourceDefinition;

/**
 * Configuration used to create JNDI contexts on a runtime.
 */
public class JndiContextDefinition extends ResourceDefinition {
    private static final long serialVersionUID = 897102744778070486L;

    private Map<String, Properties> contexts;

    public JndiContextDefinition(Map<String, Properties> contexts) {
        this.contexts = contexts;
    }

    public Map<String, Properties> getContexts() {
        return contexts;
    }
}
