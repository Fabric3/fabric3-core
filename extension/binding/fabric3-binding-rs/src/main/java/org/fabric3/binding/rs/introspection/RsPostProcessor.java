/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ServiceDefinition;
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
        ServiceDefinition<ComponentType> bindingService = null;
        if (path == null) {
            ClassLoader classLoader = implClass.getClassLoader();
            for (ServiceDefinition<ComponentType> service : componentType.getServices().values()) {
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
                for (ServiceDefinition<ComponentType> service : componentType.getServices().values()) {
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
