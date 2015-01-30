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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.fabric3.api.annotation.model.Environment;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.ProviderSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationIntrospector;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes a model provider and populates the contribution with composites it defines.
 */
@EagerInit
public class ProviderResourceProcessor implements ResourceProcessor {
    private HostInfo info;
    private Map<String, ImplementationIntrospector> introspectors = Collections.emptyMap();

    public ProviderResourceProcessor(@Reference ProcessorRegistry processorRegistry, @Reference HostInfo info) {
        this.info = info;
        processorRegistry.register(this);
    }

    @Reference(required = false)
    public void setIntrospectors(Map<String, ImplementationIntrospector> processors) {
        this.introspectors = processors;
    }

    public String getContentType() {
        return Constants.DSL_CONTENT_TYPE;
    }

    public void index(Resource resource, IntrospectionContext context) throws InstallException {
        ResourceElement<?, ?> element = resource.getResourceElements().get(0); // safe as the provider is always the first element
        ProviderSymbol symbol = (ProviderSymbol) element.getSymbol();

        Class<?> provider;
        String className = symbol.getKey();
        try {
            provider = context.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            InvalidProviderMethod error = new InvalidProviderMethod("Error loading provider class: " + className, e);
            context.addError(error);
            return;
        }
        for (Method method : provider.getDeclaredMethods()) {
            try {
                if (!method.isAnnotationPresent(Provides.class)) {
                    // skip if not a provides method
                    continue;
                }
                if (!Composite.class.equals(method.getReturnType())) {
                    InvalidProviderMethod error = new InvalidProviderMethod("Provides must return type " + Composite.class.getName() + ": " + method);
                    context.addError(error);
                    continue;
                }
                String environmentArg = null;
                if (method.getParameterTypes().length > 0) {
                    if (method.getParameterTypes().length == 1) {
                        if (method.getParameterAnnotations()[0].length != 1 || !(method.getParameterAnnotations()[0][0] instanceof Environment)) {
                            InvalidProviderMethod error = new InvalidProviderMethod("Unknown provider parameter type: " + method);
                            context.addError(error);
                            continue;
                        } else if (!method.getParameterTypes()[0].equals(String.class)) {
                            InvalidProviderMethod error = new InvalidProviderMethod("Unknown provider parameter type: " + method);
                            context.addError(error);
                            continue;
                        }
                        environmentArg = info.getEnvironment();
                    } else {
                        InvalidProviderMethod error = new InvalidProviderMethod("Provides method cannot take more than one parameter: " + method);
                        context.addError(error);
                        continue;
                    }
                }
                if (!Modifier.isStatic(method.getModifiers())) {
                    InvalidProviderMethod error = new InvalidProviderMethod("Provides method must be static: " + method);
                    context.addError(error);
                    continue;
                }
                Composite composite;
                if (environmentArg == null) {
                    composite = (Composite) method.invoke(null);
                } else {
                    composite = (Composite) method.invoke(null, environmentArg);
                }
                if (composite == null) {
                    // ignore as nothing needs to be produced
                    continue;
                }


                URI contributionUri = context.getContributionUri();
                composite.setContributionUri(contributionUri);

                // introspect definitions
                for (Component<?> definition : composite.getComponents().values()) {
                    ImplementationIntrospector introspector = introspectors.get(definition.getImplementation().getType());
                    if (introspector == null) {
                        continue;
                    }
                    definition.setContributionUri(contributionUri);
                    introspector.introspect((InjectingComponentType) definition.getComponentType(), context);
                }

                QName compositeName = composite.getName();

                QNameSymbol compositeSymbol = new QNameSymbol(compositeName);
                ResourceElement<QNameSymbol, Composite> compositeElement = new ResourceElement<>(compositeSymbol, composite);
                Contribution contribution = resource.getContribution();

                Resource compositeResource = new Resource(contribution, resource.getSource(), Constants.COMPOSITE_CONTENT_TYPE);
                compositeResource.addResourceElement(compositeElement);

                validateUnique(compositeResource, compositeElement, method, context);

                compositeResource.setState(ResourceState.PROCESSED);

                contribution.addResource(compositeResource);

            } catch (InvocationTargetException e) {
                InvalidProviderMethod error = new InvalidProviderMethod("Error invoking provides method: " + method, e.getTargetException());
                context.addError(error);
            } catch (IllegalAccessException e) {
                InvalidProviderMethod error = new InvalidProviderMethod("Error invoking provides method: " + method, e);
                context.addError(error);
            }
        }

        resource.setState(ResourceState.PROCESSED);
    }

    public void process(Resource resource, IntrospectionContext context) throws InstallException {
        // no-op
    }

    private void validateUnique(Resource resource, ResourceElement<QNameSymbol, Composite> element, Method method, IntrospectionContext context) {
        Contribution contribution = resource.getContribution();
        for (Resource entry : contribution.getResources()) {
            if (resource == entry) {
                // skip self since the resource is added to the contribution and will be iterated
                continue;
            }
            if (resource.getContentType().equals(entry.getContentType())) {
                for (ResourceElement<?, ?> elementEntry : entry.getResourceElements()) {
                    if (element.getSymbol().equals(elementEntry.getSymbol())) {
                        QName name = element.getSymbol().getKey();
                        DuplicateProviderComposite error = new DuplicateProviderComposite("Duplicate composite found with name: " + name + ":" + method);
                        context.addError(error);
                        break;
                    }
                }
            }
        }
    }

}
