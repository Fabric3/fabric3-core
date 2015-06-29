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
package org.fabric3.spi.contribution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;

import org.fabric3.api.annotation.model.Component;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Introspects a Java class and determines if it is a contribution resource, e.g. an annotated component or model provider class.
 */
public interface JavaArtifactIntrospector {

    /**
     * Introspects the class and determines if it is a contribution resource.
     *
     * @param clazz        the class
     * @param url          the URL for the class bytecode
     * @param contribution the containing contribution
     * @param context      the introspection context
     * @return a resource or null if the class is not a resource
     */
    Resource inspect(Class<?> clazz, URL url, Contribution contribution, IntrospectionContext context);

    /**
     * Returns true if the type is a concrete component by introspecting if it is annotated with {@link Component} either directly or via a meta-annotation.
     *
     * @param clazz the type
     * @return true if the type is annotated
     */
    default boolean isComponent(Class<?> clazz) {
        // if the class is abstract ignore since it is convenient to annotate a common superclass for conciseness
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        for (Annotation annotation : clazz.getAnnotations()) {
            if (Component.class.equals(annotation.annotationType())) {
                return true;
            }
            // check the meta annotations
            for (Annotation metaAnnotation : annotation.annotationType().getDeclaredAnnotations()) {
                if (Component.class.equals((metaAnnotation.annotationType()))) {
                    return true;
                }
            }

        }
        return false;
    }
}
