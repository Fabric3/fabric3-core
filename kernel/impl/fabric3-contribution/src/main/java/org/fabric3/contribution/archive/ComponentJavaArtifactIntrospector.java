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
package org.fabric3.contribution.archive;

import java.lang.reflect.Modifier;
import java.net.URL;

import org.fabric3.api.annotation.model.Component;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.JavaArtifactIntrospector;
import org.fabric3.spi.contribution.JavaSymbol;
import org.fabric3.spi.contribution.ProviderSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Introspects a class to determine if it is a Java component.
 */
public class ComponentJavaArtifactIntrospector implements JavaArtifactIntrospector {

    public Resource inspect(Class<?> clazz, URL url, Contribution contribution, IntrospectionContext context) {
        String name = clazz.getName();
        if (isProvider(name)) {
            // the class is a model provider
            UrlSource source = new UrlSource(url);
            Resource resource = new Resource(contribution, source, Constants.DSL_CONTENT_TYPE);
            ProviderSymbol symbol = new ProviderSymbol(name);
            ResourceElement<Symbol, Object> element = new ResourceElement<Symbol, Object>(symbol);
            resource.addResourceElement(element);
            return resource;
        } else if (!contribution.getManifest().isExtension()) {
            // If the class  is not annotated or it is abstract, ignore.
            // Abstract classes are ignored, since it is convenient to annotate a common superclass for conciseness.
            if (clazz.isAnnotationPresent(Component.class) && !Modifier.isAbstract(clazz.getModifiers())) {
                // class is a component
                UrlSource source = new UrlSource(url);
                Resource resource = new Resource(contribution, source, Constants.JAVA_COMPONENT_CONTENT_TYPE);
                JavaSymbol symbol = new JavaSymbol(name);
                ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, clazz);
                resource.addResourceElement(resourceElement);
                return resource;
            }
        }
        return null;
    }

    private boolean isProvider(String name) {
        return name.startsWith("f3.") && name.endsWith("Provider");
    }

}
