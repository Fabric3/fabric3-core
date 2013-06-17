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
package org.fabric3.implementation.system.introspection;

import java.util.Set;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 * Heuristic that identifies the services provided by an implementation class.
 */
public class SystemServiceHeuristic implements HeuristicProcessor {
    private final JavaContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public SystemServiceHeuristic(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void applyHeuristics(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        // if the service contracts have not already been defined then introspect them
        if (componentType.getServices().isEmpty()) {
            // get the most specific interfaces implemented by the class
            Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);

            // if the class does not implement any interfaces, then the class itself is the service contract
            // we don't have to worry about proxies because all wires to system components are optimized
            if (interfaces.isEmpty()) {
                ServiceDefinition serviceDefinition = createServiceDefinition(implClass, componentType, context);
                componentType.add(serviceDefinition);
            } else {
                // otherwise, expose all of the implemented interfaces
                for (Class<?> serviceInterface : interfaces) {
                    ServiceDefinition serviceDefinition = createServiceDefinition(serviceInterface, componentType, context);
                    componentType.add(serviceDefinition);
                }
            }
        }

    }

    private ServiceDefinition createServiceDefinition(Class<?> serviceInterface, InjectingComponentType componentType, IntrospectionContext context) {
        ServiceContract contract = contractProcessor.introspect(serviceInterface, context, componentType);
        return new ServiceDefinition(contract.getInterfaceName(), contract);
    }
}
