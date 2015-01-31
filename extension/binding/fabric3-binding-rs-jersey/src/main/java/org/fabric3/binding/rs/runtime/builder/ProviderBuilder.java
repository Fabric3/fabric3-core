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
package org.fabric3.binding.rs.runtime.builder;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.annotation.Annotation;
import java.net.URI;

import org.fabric3.binding.rs.provision.PhysicalProviderResourceDefinition;
import org.fabric3.binding.rs.runtime.bytecode.ProviderGenerator;
import org.fabric3.binding.rs.runtime.bytecode.RsReflectionHelper;
import org.fabric3.binding.rs.runtime.provider.AbstractProxyProvider;
import org.fabric3.binding.rs.runtime.provider.ProviderRegistry;
import org.fabric3.binding.rs.runtime.provider.ProxyExceptionMapper;
import org.fabric3.binding.rs.runtime.provider.ProxyMessageBodyReader;
import org.fabric3.binding.rs.runtime.provider.ProxyMessageBodyWriter;
import org.fabric3.binding.rs.runtime.provider.ProxyObjectMapperContextResolver;
import org.fabric3.binding.rs.runtime.provider.ProxyRequestFilter;
import org.fabric3.binding.rs.runtime.provider.ProxyResponseFilter;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.fabric3.spi.container.component.ComponentManager;
import org.oasisopen.sca.annotation.Reference;
import org.objectweb.asm.Type;

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

    @SuppressWarnings("unchecked")
    private Object createProvider(PhysicalProviderResourceDefinition definition) throws ContainerException {

        try {
            URI contributionUri = definition.getContributionUri();
            Class<?> providerClass = classLoaderRegistry.loadClass(contributionUri, definition.getProviderClass());

            URI filterUri = definition.getProviderUri();

            AbstractProxyProvider<?> provider;
            if (ContainerRequestFilter.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyRequestFilter.class, providerClass, null).newInstance();
            } else if (ContainerResponseFilter.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyResponseFilter.class, providerClass, null).newInstance();
            } else if (ContextResolver.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyObjectMapperContextResolver.class, providerClass, null).newInstance();
            } else if (MessageBodyReader.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyMessageBodyReader.class, providerClass, null).newInstance();
            } else if (MessageBodyWriter.class.isAssignableFrom(providerClass)) {
                provider = providerGenerator.generate(ProxyMessageBodyWriter.class, providerClass, null).newInstance();
            } else if (ExceptionMapper.class.isAssignableFrom(providerClass)) {
                String signature = getGenericExceptionMapperSignature((Class<? extends ExceptionMapper>) providerClass);
                provider = providerGenerator.generate(ProxyExceptionMapper.class, providerClass, signature).newInstance();
            } else {
                throw new ContainerException("Unknown provider type: " + providerClass.getName());
            }

            provider.init(filterUri, componentManager);
            return provider;
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            throw new ContainerException(e);
        }

    }

    /**
     * Creates the exception mapper generic signature according to the JLS.
     *
     * @param mapperClass the ExceptionMapper class
     * @return the signature
     * @throws ContainerException if there is an error creating the signature
     */
    private String getGenericExceptionMapperSignature(Class<? extends ExceptionMapper> mapperClass) throws ContainerException {
        Class<?> exceptionType = RsReflectionHelper.getExceptionType(mapperClass);
        String exceptionName = Type.getInternalName(exceptionType);
        return "<E:L" + exceptionName + ";>L" + Type.getInternalName(ProxyExceptionMapper.class) + "<L" + exceptionName + ";>;L" + Type.getInternalName(
                ExceptionMapper.class) + "<TE;>;";
    }

}
