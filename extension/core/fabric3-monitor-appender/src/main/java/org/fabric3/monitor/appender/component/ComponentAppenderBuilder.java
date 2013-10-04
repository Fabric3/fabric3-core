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
package org.fabric3.monitor.appender.component;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

import org.fabric3.host.Names;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderBuilder;
import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Instantiates a component based appender from a {@link PhysicalComponentAppenderDefinition}.
 */
@EagerInit
public class ComponentAppenderBuilder implements AppenderBuilder<PhysicalComponentAppenderDefinition> {
    private ComponentManager componentManager;

    public ComponentAppenderBuilder(@Reference ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    public Appender build(PhysicalComponentAppenderDefinition definition) throws BuilderException {
        URI uri = URI.create(Names.RUNTIME_NAME + "/" + definition.getComponentName());

        Component component = componentManager.getComponent(uri);
        if (component == null) {
            throw new BuilderException("Component not found: " + uri);
        }
        if (!(component instanceof AtomicComponent)) {
            throw new BuilderException("Component must be atomic: " + uri);
        }
        AtomicComponent atomicComponent = (AtomicComponent) component;
        return new AppenderAdapter(atomicComponent);
    }

    /**
     * Wraps the appender to allow lazy-initialization.
     */
    private class AppenderAdapter implements Appender {
        private AtomicComponent atomicComponent;
        private Appender delegate;

        private AppenderAdapter(AtomicComponent atomicComponent) {
            this.atomicComponent = atomicComponent;
        }

        public void start() throws IOException {
            try {
                Object instance = atomicComponent.getInstance();
                if (!(instance instanceof Appender)) {
                    throw new IOException("Component does not implement " + Appender.class.getName() + ": " + atomicComponent.getUri());
                }
                delegate = (Appender) instance;
            } catch (InstanceLifecycleException e) {
                throw new IOException(e);
            }
        }

        public void stop() throws IOException {
        }

        public void write(ByteBuffer buffer) throws IOException {
            delegate.write(buffer);
        }
    }
}
