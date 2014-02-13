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
package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.fabric3.api.annotation.model.Environment;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.Composite;
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
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes a model provider and populates the contribution with composites it defines.
 */
@EagerInit
public class ProviderResourceProcessor implements ResourceProcessor {
    private HostInfo info;

    public ProviderResourceProcessor(@Reference ProcessorRegistry processorRegistry, @Reference HostInfo info) {
        this.info = info;
        processorRegistry.register(this);
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
                    InvalidProviderMethod error = new InvalidProviderMethod("Provides method returned null: " + method);
                    context.addError(error);
                    continue;
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

            } catch (InvocationTargetException | IllegalAccessException e) {
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
