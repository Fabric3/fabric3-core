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
package org.fabric3.jndi.runtime;

import javax.naming.NamingException;
import java.util.Map;
import java.util.Properties;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.jndi.provision.PhysicalJndiContextDefinition;
import org.fabric3.jndi.spi.JndiContextManager;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates and registers JNDI contexts with the runtime {@link JndiContextManager}.
 */
@EagerInit
public class JndiContextBuilder implements ResourceBuilder<PhysicalJndiContextDefinition> {
    private JndiContextManager manager;

    public JndiContextBuilder(@Reference JndiContextManager manager) {
        this.manager = manager;
    }

    public void build(PhysicalJndiContextDefinition definition) throws Fabric3Exception {
        for (Map.Entry<String, Properties> entry : definition.getContexts().entrySet()) {
            try {
                manager.register(entry.getKey(), entry.getValue());
            } catch (NamingException e) {
                throw new Fabric3Exception(e);
            }
        }
    }

    public void remove(PhysicalJndiContextDefinition definition) throws Fabric3Exception {
        for (String name : definition.getContexts().keySet()) {
            try {
                manager.unregister(name);
            } catch (NamingException e) {
                throw new Fabric3Exception(e);
            }
        }
    }
}