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
*/
package org.fabric3.implementation.spring.runtime.builder;

import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.spring.provision.SpringConnectionTargetDefinition;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.implementation.spring.runtime.component.SpringEventStreamHandler;
import org.fabric3.spi.container.builder.component.ConnectionAttachException;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.type.java.JavaType;

/**
 * Attaches and detaches a {@link ChannelConnection} from a Spring component consumer.
 */
@EagerInit
public class SpringTargetConnectionAttacher implements TargetConnectionAttacher<SpringConnectionTargetDefinition> {
    private ComponentManager manager;

    public SpringTargetConnectionAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(PhysicalConnectionSourceDefinition source, SpringConnectionTargetDefinition target, ChannelConnection connection)
            throws ConnectionAttachException {
        URI targetUri = target.getTargetUri();
        SpringComponent component = (SpringComponent) manager.getComponent(targetUri);
        if (component == null) {
            throw new ConnectionAttachException("Target component not found: " + targetUri);
        }
        String beanName = target.getBeanName();
        JavaType<?> type = target.getType();
        String consumerName = target.getMethodName();
        SpringEventStreamHandler handler = new SpringEventStreamHandler(beanName, consumerName, type, component);
        EventStream stream = connection.getEventStream();
        stream.addHandler(handler);
    }

    public void detach(PhysicalConnectionSourceDefinition source, SpringConnectionTargetDefinition target) throws ConnectionAttachException {
        // no-op
    }

}