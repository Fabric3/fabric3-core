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
package org.fabric3.binding.jms.generator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.JmsBinding;
import org.fabric3.api.model.type.component.BindingHandler;
import org.fabric3.binding.jms.common.JmsConnectionConstants;
import org.fabric3.binding.jms.spi.provision.SessionType;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;

/**
 * Contains helper functions used during generation.
 */
public class JmsGeneratorHelper {

    /**
     * Converts a URI to a JMS subscription id format. Hierarchical URIs are converted to dot notation by replacing '/' with '.'.
     *
     * @param uri the URI
     * @return the specifier
     */
    public static String getSubscriptionId(URI uri) {
        String id = uri.getPath().substring(1).replace("/", ".");
        String fragment = uri.getFragment();
        if (fragment != null) {
            id = id + "." + fragment;
        }
        return id;
    }

    /**
     * Generates a default connection factory configuration if a factory or class name is not configured.
     *
     * @param factory     the connection factory definition to configure
     * @param sessionType the session type
     */
    public static void generateDefaultFactoryConfiguration(ConnectionFactoryDefinition factory, SessionType sessionType) {
        if (factory.getName() == null && !factory.getProperties().containsKey("class")) {
            if (SessionType.GLOBAL_TRANSACTED == sessionType) {
                factory.setName(JmsConnectionConstants.DEFAULT_XA_CONNECTION_FACTORY);
            } else {
                factory.setName(JmsConnectionConstants.DEFAULT_CONNECTION_FACTORY);
            }
        }
    }

    public static List<PhysicalBindingHandlerDefinition> generateBindingHandlers(URI domainUri, JmsBinding definition) {
        List<PhysicalBindingHandlerDefinition> handlers = new ArrayList<>();
        for (BindingHandler handlerDefinition : definition.getHandlers()) {
            // URIs specified in handler elements in a composite are relative and must be made absolute
            URI resolvedUri = URI.create(domainUri.toString() + "/" + handlerDefinition.getTarget());
            handlers.add(new PhysicalBindingHandlerDefinition(resolvedUri));
        }
        return handlers;
    }

    private JmsGeneratorHelper() {
    }

}
