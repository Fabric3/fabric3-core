/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.generator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingDefinition;
import org.fabric3.binding.jms.spi.provision.SessionType;
import org.fabric3.api.model.type.component.BindingHandlerDefinition;
import org.fabric3.binding.jms.common.JmsConnectionConstants;
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

    public static List<PhysicalBindingHandlerDefinition> generateBindingHandlers(URI domainUri, JmsBindingDefinition definition) {
        List<PhysicalBindingHandlerDefinition> handlers = new ArrayList<>();
        for (BindingHandlerDefinition handlerDefinition : definition.getHandlers()) {
            // URIs specified in handler elements in a composite are relative and must be made absolute
            URI resolvedUri = URI.create(domainUri.toString() + "/" + handlerDefinition.getTarget());
            handlers.add(new PhysicalBindingHandlerDefinition(resolvedUri));
        }
        return handlers;
    }

    private JmsGeneratorHelper() {
    }

}
