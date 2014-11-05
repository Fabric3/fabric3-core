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
package org.fabric3.binding.rs.runtime.builder;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.annotation.Annotation;
import java.net.URI;

import org.fabric3.binding.rs.provision.PhysicalProviderResourceDefinition;
import org.fabric3.binding.rs.runtime.bytecode.ProviderGenerator;
import org.fabric3.binding.rs.runtime.provider.AbstractProxyProvider;
import org.fabric3.binding.rs.runtime.provider.ProviderRegistry;
import org.fabric3.binding.rs.runtime.provider.ProxyMessageBodyReader;
import org.fabric3.binding.rs.runtime.provider.ProxyMessageBodyWriter;
import org.fabric3.binding.rs.runtime.provider.ProxyObjectMapperContextResolver;
import org.fabric3.binding.rs.runtime.provider.ProxyRequestFilter;
import org.fabric3.binding.rs.runtime.provider.ProxyResponseFilter;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.fabric3.spi.container.component.ComponentManager;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ProviderBuilder implements ResourceBuilder<PhysicalProviderResourceDefinition> {
    private ProviderRegistry providerRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private ComponentManager componentManager;
    private ProviderGenerator providerGenerator;

    public ProviderBuilder(@Reference ProviderRegistry providerRegistry,
                           @Reference ClassLoaderRegistry classLoaderRegistry,
                           @Reference ComponentManager componentManager,
                           @Reference ProviderGenerator providerGenerator) {
        this.providerRegistry = providerRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.componentManager = componentManager;
        this.providerGenerator = providerGenerator;
    }

    @SuppressWarnings("unchecked")
    public void build(PhysicalProviderResourceDefinition definition) throws ContainerException {
        try {
            URI providerUri = definition.getProviderUri();

            Object provider = createProvider(definition);
            if (definition.getBindingAnnotation() != null) {
                String bindingAnnotation = definition.getBindingAnnotation();
                URI contributionUri = definition.getContributionUri();
                Class<Annotation> annotationClass = (Class<Annotation>) classLoaderRegistry.loadClass(contributionUri, bindingAnnotation);
                providerRegistry.registerNameFilter(providerUri, annotationClass, provider);
            } else {
                providerRegistry.registerGlobalProvider(providerUri, provider);
            }
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void remove(PhysicalProviderResourceDefinition definition) throws ContainerException {
        try {
            if (definition.getBindingAnnotation() != null) {
                String bindingAnnotation = definition.getBindingAnnotation();
                URI contributionUri = definition.getContributionUri();
                Class<Annotation> annotationClass = (Class<Annotation>) classLoaderRegistry.loadClass(contributionUri, bindingAnnotation);
                URI filterUri = definition.getProviderUri();
                providerRegistry.unregisterNameFilter(filterUri, annotationClass);
            } else {
                URI filterUri = definition.getProviderUri();
                providerRegistry.unregisterGlobalFilter(filterUri);
            }
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }

    private Object createProvider(PhysicalProviderResourceDefinition definition) throws ContainerException {

        try {
            URI contributionUri = definition.getContributionUri();
            Class<?> providerClass = classLoaderRegistry.loadClass(contributionUri, definition.getProviderClass());

            URI filterUri = definition.getProviderUri();

            AbstractProxyProvider<?> provider;
            if (ContainerRequestFilter.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyRequestFilter.class, providerClass).newInstance();
            } else if (ContainerResponseFilter.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyResponseFilter.class, providerClass).newInstance();
            } else if (ContextResolver.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyObjectMapperContextResolver.class, providerClass).newInstance();
            } else if (MessageBodyReader.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyMessageBodyReader.class, providerClass).newInstance();
            } else if (MessageBodyWriter.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyMessageBodyWriter.class, providerClass).newInstance();
            } else {
                throw new ContainerException("Unknown provider type: " + providerClass.getName());
            }

            provider.init(filterUri, componentManager);
            return provider;
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            throw new ContainerException(e);
        }

    }

}
