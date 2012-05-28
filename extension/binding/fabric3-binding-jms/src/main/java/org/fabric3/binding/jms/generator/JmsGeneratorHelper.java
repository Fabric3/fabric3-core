/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.binding.jms.spi.runtime.JmsConstants;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.type.binding.BindingHandlerDefinition;

import static org.fabric3.binding.jms.spi.runtime.JmsConstants.DEFAULT_XA_CONNECTION_FACTORY;

/**
 * Contains helper functions used during generation.
 *
 * @version $Revision$ $Date$
 */
public class JmsGeneratorHelper {

    /**
     * Converts a URI to a JMS specifier that can be used for creating client id or connection factory name for the source side of a wire or channel
     * connection. Hierarchical URIs are converted to dot notation by replacing '/' with '.'.
     *
     * @param uri the URI
     * @return the specifier
     */
    public static String getSourceSpecifier(URI uri) {
        return getSpecifier(uri) + "Source";
    }

    /**
     * Converts a URI to a JMS specifier that can be used for creating client id or connection factory name for the target side of a wire or channel
     * connection. Hierarchical URIs are converted to dot notation by replacing '/' with '.'.
     *
     * @param uri the URI
     * @return the specifier
     */
    public static String getTargetSpecifier(URI uri) {
        return getSpecifier(uri) + "Target";
    }


    /**
     * Generates a default connection factory configuration.
     *
     * @param factory   the connection factory definition to configure
     * @param specifier the factory specifier used to generate a unique name
     * @param trxType   the transaction type
     */
    public static void generateDefaultFactoryConfiguration(ConnectionFactoryDefinition factory, String specifier, TransactionType trxType) {
        if (factory.getName() == null && factory.getTemplateName() == null) {
            factory.setName(specifier);
            if (TransactionType.GLOBAL == trxType) {
                factory.setTemplateName(DEFAULT_XA_CONNECTION_FACTORY);
            } else {
                factory.setTemplateName(JmsConstants.DEFAULT_CONNECTION_FACTORY);
            }
        } else if (factory.getTemplateName() != null) {
            factory.setName(specifier);
        }
    }

    public static List<PhysicalBindingHandlerDefinition> generateBindingHandlers(URI domainUri, JmsBindingDefinition definition) {
        List<PhysicalBindingHandlerDefinition> handlers = new ArrayList<PhysicalBindingHandlerDefinition>();
        for (BindingHandlerDefinition handlerDefinition : definition.getHandlers()) {
            // URIs specified in handler elements in a composite are relative and must be made absolute
            URI resolvedUri = URI.create(domainUri.toString() + "/" + handlerDefinition.getTarget());
            handlers.add(new PhysicalBindingHandlerDefinition(resolvedUri));
        }
        return handlers;
    }

    private static String getSpecifier(URI uri) {
        String specifier = uri.getPath().substring(1).replace("/", ".");
        String fragment = uri.getFragment();
        if (fragment != null) {
            specifier = specifier + "." + fragment;
        }
        return specifier;
    }


    private JmsGeneratorHelper() {
    }

}
