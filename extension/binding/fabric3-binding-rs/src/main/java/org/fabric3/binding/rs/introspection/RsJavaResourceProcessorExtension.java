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

import javax.ws.rs.NameBinding;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.binding.rs.model.ProviderResourceDefinition;
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

    public void process(ComponentDefinition<JavaImplementation> definition) {
        try {
            URI contributionUri = definition.getContributionUri();
            String implClass = definition.getImplementation().getImplementationClass();
            Class<?> clazz = classLoaderRegistry.loadClass(contributionUri, implClass);
            if (!(ContainerRequestFilter.class.isAssignableFrom(clazz)) && !ContainerResponseFilter.class.isAssignableFrom(clazz)
                && !ContextResolver.class.isAssignableFrom(clazz) && !MessageBodyReader.class.isAssignableFrom(clazz)
                && !MessageBodyWriter.class.isAssignableFrom(clazz)) {
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
            String name = definition.getName();
            ProviderResourceDefinition resourceDefinition = new ProviderResourceDefinition(name, bindingAnnotation, implClass, contributionUri);
            definition.getParent().add(resourceDefinition);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);   // will not happen
        }
    }

}
