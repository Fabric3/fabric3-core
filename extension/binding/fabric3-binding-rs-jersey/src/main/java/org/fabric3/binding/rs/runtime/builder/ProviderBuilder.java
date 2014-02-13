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
import java.lang.annotation.Annotation;
import java.net.URI;

import org.fabric3.binding.rs.provision.PhysicalProviderResourceDefinition;
import org.fabric3.binding.rs.runtime.bytecode.ProviderGenerator;
import org.fabric3.binding.rs.runtime.filter.AbstractProxyFilter;
import org.fabric3.binding.rs.runtime.filter.FilterRegistry;
import org.fabric3.binding.rs.runtime.filter.ProxyRequestFilter;
import org.fabric3.binding.rs.runtime.filter.ProxyResponseFilter;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.fabric3.spi.container.component.ComponentManager;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ProviderBuilder implements ResourceBuilder<PhysicalProviderResourceDefinition> {
    private FilterRegistry filterRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private ComponentManager componentManager;
    private ProviderGenerator providerGenerator;

    public ProviderBuilder(@Reference FilterRegistry filterRegistry,
                           @Reference ClassLoaderRegistry classLoaderRegistry,
                           @Reference ComponentManager componentManager,
                           @Reference ProviderGenerator providerGenerator) {
        this.filterRegistry = filterRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.componentManager = componentManager;
        this.providerGenerator = providerGenerator;
    }

    @SuppressWarnings("unchecked")
    public void build(PhysicalProviderResourceDefinition definition) throws BuilderException {
        try {
            URI filterUri = definition.getProviderUri();

            Object filter = createFilter(definition);
            if (definition.getBindingAnnotation() != null) {
                String bindingAnnotation = definition.getBindingAnnotation();
                URI contributionUri = definition.getContributionUri();
                Class<Annotation> annotationClass = (Class<Annotation>) classLoaderRegistry.loadClass(contributionUri, bindingAnnotation);
                filterRegistry.registerNameFilter(filterUri, annotationClass, filter);
            } else {
                filterRegistry.registerGlobalFilter(filterUri, filter);
            }
        } catch (ClassNotFoundException e) {
            throw new BuilderException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void remove(PhysicalProviderResourceDefinition definition) throws BuilderException {
        try {
            if (definition.getBindingAnnotation() != null) {
                String bindingAnnotation = definition.getBindingAnnotation();
                URI contributionUri = definition.getContributionUri();
                Class<Annotation> annotationClass = (Class<Annotation>) classLoaderRegistry.loadClass(contributionUri, bindingAnnotation);
                URI filterUri = definition.getProviderUri();
                filterRegistry.unregisterNameFilter(filterUri, annotationClass);
            } else {
                URI filterUri = definition.getProviderUri();
                filterRegistry.unregisterGlobalFilter(filterUri);
            }
        } catch (ClassNotFoundException e) {
            throw new BuilderException(e);
        }
    }

    private Object createFilter(PhysicalProviderResourceDefinition definition) {

        try {
            URI contributionUri = definition.getContributionUri();
            Class<?> filterClass = classLoaderRegistry.loadClass(contributionUri, definition.getProviderClass());

            URI filterUri = definition.getProviderUri();
            AbstractProxyFilter<?> filter;
            if (ContainerRequestFilter.class.isAssignableFrom(filterClass)) {
                filter = providerGenerator.generate(ProxyRequestFilter.class, filterClass).newInstance();
            } else {
                filter = providerGenerator.generate(ProxyResponseFilter.class, filterClass).newInstance();
            }
            filter.init(filterUri, componentManager);
            return filter;
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            throw new AssertionError(e);
        }

    }

}
