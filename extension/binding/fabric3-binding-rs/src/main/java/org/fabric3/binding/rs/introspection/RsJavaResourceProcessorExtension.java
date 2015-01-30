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

import javax.ws.rs.NameBinding;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.binding.rs.model.ProviderResource;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.contribution.JavaResourceProcessorExtension;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class RsJavaResourceProcessorExtension implements JavaResourceProcessorExtension {
    private ClassLoaderRegistry classLoaderRegistry;
    private MonitorChannel monitor;

    public RsJavaResourceProcessorExtension(@Reference ClassLoaderRegistry classLoaderRegistry, @Monitor MonitorChannel monitor) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.monitor = monitor;
    }

    public void process(Component<JavaImplementation> component) {
        try {
            URI contributionUri = component.getContributionUri();
            String implClass = component.getImplementation().getImplementationClass();
            Class<?> clazz = classLoaderRegistry.loadClass(contributionUri, implClass);
            if (!(ContainerRequestFilter.class.isAssignableFrom(clazz)) && !ContainerResponseFilter.class.isAssignableFrom(clazz)
                && !ContextResolver.class.isAssignableFrom(clazz) && !MessageBodyReader.class.isAssignableFrom(clazz)
                && !MessageBodyWriter.class.isAssignableFrom(clazz) && !ExceptionMapper.class.isAssignableFrom(clazz)) {
                // not a provider type
                return;
            }
            if (ContextResolver.class.isAssignableFrom(clazz)) {
                // currently only object mappers are supported
                Type[] interfaces = clazz.getGenericInterfaces();
                for (Type interfaze : interfaces) {
                    if (!(interfaze instanceof ParameterizedType)) {
                        continue;
                    }
                    ParameterizedType parameterizedType = (ParameterizedType) interfaze;
                    if (ContextResolver.class.equals(parameterizedType.getRawType())) {
                        Type[] arguments = parameterizedType.getActualTypeArguments();
                        if (arguments.length != 1 || !ObjectMapper.class.equals(arguments[0])) {
                            monitor.severe("Only ObjectMapper JAX-RS ContextResolver types are supported. The class must implement " +
                                           "ContextResolver<ObjectMapper>. Ignoring provider: " + implClass);
                            return;
                        }
                    }
                }

            }
            String bindingAnnotation = null;
            for (Annotation annotation : clazz.getAnnotations()) {
                Class<? extends Annotation> type = annotation.annotationType();
                if (type.isAnnotationPresent(NameBinding.class)) {
                    bindingAnnotation = type.getName();
                    break;
                }
            }
            String name = component.getName();
            ProviderResource providerResource = new ProviderResource(name, bindingAnnotation, implClass, contributionUri);
            component.getParent().add(providerResource);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);   // will not happen
        }
    }

}
