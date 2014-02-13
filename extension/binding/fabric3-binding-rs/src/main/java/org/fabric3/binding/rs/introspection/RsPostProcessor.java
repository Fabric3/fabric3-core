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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.api.annotation.model.EndpointUri;
import org.fabric3.api.binding.rs.model.RsBindingDefinition;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.PostProcessor;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class RsPostProcessor implements PostProcessor {
    private Set<Class<?>> annotations = new HashSet<>();

    public RsPostProcessor() {
        annotations.add(Path.class);
        annotations.add(GET.class);
        annotations.add(DELETE.class);
        annotations.add(POST.class);
        annotations.add(PUT.class);
        annotations.add(HEAD.class);
    }

    public void process(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Path path = implClass.getAnnotation(Path.class);
        AbstractService<?> bindingService = null;
        if (path == null) {
            ClassLoader classLoader = implClass.getClassLoader();
            for (AbstractService service : componentType.getServices().values()) {
                try {
                    Class<?> interfaze = classLoader.loadClass(service.getServiceContract().getQualifiedInterfaceName());
                    path = interfaze.getAnnotation(Path.class);
                    if (path != null) {
                        bindingService = service;
                        break;
                    }

                } catch (ClassNotFoundException e) {
                    // cannot happen
                    throw new AssertionError(e);
                }
            }
            if (path == null) {
                return;
            }
        } else {
            // impl class is annotated, find the interface to apply the binding to
            if (componentType.getServices().isEmpty()) {
                return;
            } else if (componentType.getServices().size() == 1) {
                // a single service
                bindingService = componentType.getServices().values().iterator().next();
            } else {
                // more than one service, find the one to use
                Class<?> bindingInterface = findBindingInterface(implClass);
                if (bindingInterface == null) {
                    // interface not found
                    return;
                }
                for (AbstractService service : componentType.getServices().values()) {
                    if (service.getServiceContract().getQualifiedInterfaceName().equals(bindingInterface.getName())) {
                        bindingService = service;
                        break;
                    }
                }
            }
        }
        EndpointUri endpointUri = implClass.getAnnotation(EndpointUri.class);
        String serviceName = bindingService.getName();

        String base = serviceName;
        if (endpointUri != null) {
            base = endpointUri.value();
        }

        RsBindingDefinition binding = new RsBindingDefinition(serviceName, URI.create("/" + base));
        bindingService.addBinding(binding);
    }

    /**
     * Finds the interface associated with the methods on the implementation class that contains the JAX-RS annotations.
     *
     * @param implClass the implementation class
     * @return the interface or null if one is not found
     */
    private Class<?> findBindingInterface(Class<?> implClass) {
        Class<?> bindingInterface = null;
        for (Method method : implClass.getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotations.contains(annotation.annotationType())) {
                    // found a JAX-RS method, find the interface
                    for (Class<?> interfaze : implClass.getInterfaces()) {
                        try {
                            interfaze.getMethod(method.getName(), method.getParameterTypes());
                            bindingInterface = interfaze;
                            break;
                        } catch (NoSuchMethodException e) {
                            // ignore
                        }
                    }
                }
            }
        }
        return bindingInterface;
    }
}
