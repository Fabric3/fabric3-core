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
package org.fabric3.implementation.junit.introspection;

import javax.xml.namespace.QName;
import java.net.URL;

import org.fabric3.api.Namespaces;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.JavaArtifactIntrospector;
import org.fabric3.spi.contribution.JavaSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.junit.runner.RunWith;

/**
 * Creates a resource for classes that are JUnit components.
 */
public class JUnitArtifactIntrospector implements JavaArtifactIntrospector {
    private static final QName TEST_COMPOSITE = new QName(Namespaces.F3, "TestComposite");

    public Resource inspect(Class<?> clazz, URL url, Contribution contribution, IntrospectionContext context) {
        if (contribution.getManifest().isExtension()) {
            return null;
        }
        if (isComponent(clazz) || !clazz.isAnnotationPresent(RunWith.class)) {
            // not a Junit component or labeled as a component, avoid creating a duplicate
            return null;
        }
        UrlSource source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, Constants.JAVA_COMPONENT_CONTENT_TYPE);
        JavaSymbol symbol = new JavaSymbol(clazz.getName());
        ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, clazz);
        resourceElement.setMetadata(TEST_COMPOSITE);
        resource.addResourceElement(resourceElement);
        return resource;
    }
}
