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

import javax.ws.rs.ext.Provider;
import java.net.URL;

import org.fabric3.api.annotation.model.Component;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.JavaArtifactIntrospector;
import org.fabric3.spi.contribution.JavaSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Creates a component resource for classes annotated with {link @Provider}. If a class is also annotated with {@link Component} it will be skipped as the
 * default introspector will be triggered.
 */
@EagerInit
public class RsProviderIntrospector implements JavaArtifactIntrospector {

    public Resource inspect(Class<?> clazz, URL url, Contribution contribution, IntrospectionContext context) {
        if (!clazz.isAnnotationPresent(Provider.class) || clazz.isAnnotationPresent(Component.class)) {
            // not a provider or already configured as a component
            return null;
        }

        UrlSource source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, Constants.JAVA_COMPONENT_CONTENT_TYPE);
        JavaSymbol symbol = new JavaSymbol(clazz.getName());
        ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, clazz);
        resource.addResourceElement(resourceElement);
        return resource;
    }

}
