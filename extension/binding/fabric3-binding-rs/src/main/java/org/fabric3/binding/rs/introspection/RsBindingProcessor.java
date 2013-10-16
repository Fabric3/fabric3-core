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
package org.fabric3.binding.rs.introspection;

import javax.ws.rs.Path;
import java.net.URI;

import org.fabric3.api.binding.rs.RsBindingDefinition;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentService;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.processor.BindingProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Adds RS binding metadata to un-managed instances with JAX-RS annotations deployed to a domain.
 */
@EagerInit
public class RsBindingProcessor implements BindingProcessor {
    public void process(ComponentDefinition<?> definition, IntrospectionContext context) {
        if (!(definition.getImplementation() instanceof JavaImplementation)) {
            return;
        }
        JavaImplementation implementation = (JavaImplementation) definition.getImplementation();
        Object instance = implementation.getInstance();
        if (instance == null) {
            return;
        }
        for (ComponentService service : definition.getServices().values()) {
            if (!service.getBindings().isEmpty()) {
                // a binding is already specified
                return;
            }
        }
        Class<?> rsInterface = null;
        JavaServiceContract rsContract = null;
        ComponentType componentType = definition.getComponentType();
        for (AbstractService service : componentType.getServices().values()) {
            if (!(service.getServiceContract() instanceof JavaServiceContract)) {
                continue;
            }
            JavaServiceContract contract = (JavaServiceContract) service.getServiceContract();
            try {
                Class<?> interfaze = instance.getClass().getClassLoader().loadClass(contract.getInterfaceClass());
                if (interfaze.isAnnotationPresent(Path.class)) {
                    rsInterface = interfaze;
                    rsContract = contract;
                    break;
                }
            } catch (ClassNotFoundException e) {
                // cannot happen
                throw new AssertionError(e);
            }
        }
        if (rsInterface == null) {
            // not an REST service
            return;
        }
        String serviceName = rsInterface.getSimpleName();
        ComponentService componentService = definition.getServices().get(serviceName);
        if (componentService == null) {
            componentService = new ComponentService(serviceName);
            componentService.setServiceContract(rsContract);
            definition.add(componentService);
        }
        RsBindingDefinition binding = new RsBindingDefinition(serviceName, URI.create("/" + serviceName));
        componentService.addBinding(binding);

    }
}
