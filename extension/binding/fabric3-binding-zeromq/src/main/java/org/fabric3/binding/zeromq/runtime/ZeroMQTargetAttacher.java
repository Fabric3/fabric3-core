/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime;

import java.net.URI;
import java.util.List;

import org.fabric3.binding.zeromq.provision.ZeroMQWireTargetDefinition;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;

/**
 *
 */
public class ZeroMQTargetAttacher implements TargetWireAttacher<ZeroMQWireTargetDefinition> {
    private ZeroMQWireBroker broker;
    private ClassLoaderRegistry registry;

    public ZeroMQTargetAttacher(@Reference ZeroMQWireBroker broker, @Reference ClassLoaderRegistry registry) {
        this.broker = broker;
        this.registry = registry;
    }

    public void attach(PhysicalWireSourceDefinition source, ZeroMQWireTargetDefinition target, Wire wire) throws BuilderException {
        final URI sourceUri = source.getUri();
        String id = sourceUri.getPath().substring(1) + "/" + sourceUri.getFragment();   // strip leading '/'
        URI targetUri = target.getUri();
        ClassLoader loader = registry.getClassLoader(target.getClassLoaderId());
        List<InvocationChain> chains = ZeroMQAttacherHelper.sortChains(wire);
        try {
            broker.connectToSender(id, targetUri, chains, target.getMetadata(), loader);
        } catch (BrokerException e) {
            throw new BuilderException(e);
        }
    }

    public void detach(PhysicalWireSourceDefinition source, ZeroMQWireTargetDefinition target) throws BuilderException {
        String id = source.getUri().toString();
        URI uri = target.getUri();
        try {
            broker.releaseSender(id, uri);
        } catch (BrokerException e) {
            throw new BuilderException(e);
        }
    }

    public ObjectFactory<?> createObjectFactory(ZeroMQWireTargetDefinition target) throws BuilderException {
        throw new UnsupportedOperationException();
    }


}
