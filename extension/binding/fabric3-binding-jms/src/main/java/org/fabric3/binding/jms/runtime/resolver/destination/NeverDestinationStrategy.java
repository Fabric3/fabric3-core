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
package org.fabric3.binding.jms.runtime.resolver.destination;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import java.util.List;

import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.binding.jms.runtime.resolver.DestinationStrategy;
import org.fabric3.binding.jms.spi.runtime.provider.DestinationResolver;
import org.fabric3.binding.jms.spi.runtime.provider.JmsResolutionException;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation that always resolves a destination against JNDI and never attempts to create it.
 */
public class NeverDestinationStrategy implements DestinationStrategy {
    private List<DestinationResolver> resolvers;

    @Reference(required = false)
    public void setResolvers(List<DestinationResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public Destination getDestination(DestinationDefinition definition, ConnectionFactory factory) throws JmsResolutionException {
        for (DestinationResolver resolver : resolvers) {
            Destination destination = resolver.resolve(definition);
            if (destination != null) {
                return destination;
            }
        }
        throw new JmsResolutionException("Destination not found: " + definition.getName());
    }

}
