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
package org.fabric3.binding.jms.runtime;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.jms.runtime.resolver.AdministeredObjectResolver;
import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.DeliveryMode;
import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.common.HeadersDefinition;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.provision.JmsConnectionTargetDefinition;
import org.fabric3.binding.jms.spi.runtime.JmsResolutionException;
import org.fabric3.spi.builder.component.ConnectionAttachException;
import org.fabric3.spi.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;

/**
 * Attaches a producer to a JMS destination.
 *
 * @version $Revision$ $Date$
 */
public class JmsConnectionTargetAttacher implements TargetConnectionAttacher<JmsConnectionTargetDefinition> {
    private AdministeredObjectResolver resolver;

    public JmsConnectionTargetAttacher(@Reference AdministeredObjectResolver resolver) {
        this.resolver = resolver;
    }

    public void attach(PhysicalConnectionSourceDefinition source, JmsConnectionTargetDefinition target, ChannelConnection connection)
            throws ConnectionAttachException {
        // resolve the connection factories and destinations for the wire
        JmsBindingMetadata metadata = target.getMetadata();
        ConnectionFactoryDefinition connectionFactoryDefinition = metadata.getConnectionFactory();
        HeadersDefinition headers = metadata.getHeaders();
        boolean persistent = DeliveryMode.PERSISTENT == headers.getDeliveryMode()  || headers.getDeliveryMode() == null;
        Destination destination;
        ConnectionFactory connectionFactory;
        try {
            connectionFactory = resolver.resolve(connectionFactoryDefinition);
            DestinationDefinition destinationDefinition = metadata.getDestination();
            destination = resolver.resolve(destinationDefinition, connectionFactory);
        } catch (JmsResolutionException e) {
            throw new ConnectionAttachException(e);
        }
        for (EventStream stream : connection.getEventStreams()) {
            JmsEventStreamHandler handler = new JmsEventStreamHandler(destination, connectionFactory, persistent);
            stream.addHandler(handler);
        }

    }

    public void detach(PhysicalConnectionSourceDefinition source, JmsConnectionTargetDefinition target) throws ConnectionAttachException {
        try {
            resolver.release(target.getMetadata().getConnectionFactory());
        } catch (JmsResolutionException e) {
            throw new ConnectionAttachException(e);
        }
    }

}