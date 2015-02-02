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
package org.fabric3.implementation.system.introspection;

import java.lang.reflect.Constructor;

import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.NoConstructorFound;
import org.fabric3.spi.introspection.java.annotation.AmbiguousConstructor;

/**
 * Heuristic that selects the constructor to use.
 */
public class SystemConstructorHeuristic implements HeuristicProcessor {

    public void applyHeuristics(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        // if there is already a defined constructor then do nothing
        if (componentType.getConstructor() != null) {
            return;
        }

        Constructor<?> ctor = findConstructor(implClass, componentType, context);
        componentType.setConstructor(ctor);
    }

    /**
     * Find the constructor to use. <p/> For now, we require that the class have a single constructor or one annotated with @Constructor. If there is more than
     * one, the default constructor will be selected or an org.osoa.sca.annotations.Constructor annotation must be used.
     *
     * @param implClass     the class we are inspecting
     * @param componentType the parent component type
     * @param context       the introspection context to report errors and warnings
     * @return the constructor to use
     */
    Constructor<?> findConstructor(Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        Constructor<?>[] constructors = implClass.getDeclaredConstructors();
        Constructor<?> selected = null;
        if (constructors.length == 1) {
            selected = constructors[0];
        } else {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(org.oasisopen.sca.annotation.Constructor.class)) {
                    if (selected != null) {
                        context.addError(new AmbiguousConstructor(implClass, componentType));
                        return null;
                    }
                    selected = constructor;
                }
            }
            if (selected == null) {
                try {
                    selected = implClass.getConstructor();
                } catch (NoSuchMethodException e) {
                    context.addError(new NoConstructorFound(implClass, componentType));
                    return null;
                }
            }
        }
        return selected;
    }

}