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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.introspection.java.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Abstract base class for annotation processors that provides default implementations of the interface methods that simply return.
 */
public abstract class AbstractAnnotationProcessor<A extends Annotation> implements AnnotationProcessor<A> {
    private final Class<A> type;

    /**
     * Constructor binding the annotation type.
     *
     * @param type the annotation type
     */
    protected AbstractAnnotationProcessor(Class<A> type) {
        this.type = type;
    }

    public Class<A> getType() {
        return type;
    }

    public void visitType(A annotation, Class<?> type, InjectingComponentType componentType, IntrospectionContext context) {
    }

    public void visitField(A annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
    }

    public void visitMethod(A annotation, Method method, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
    }

    public void visitMethodParameter(A annotation,
                                     Method method,
                                     int index,
                                     Class<?> implClass,
                                     InjectingComponentType componentType,
                                     IntrospectionContext context) {
    }

    public void visitConstructor(A annotation,
                                 Constructor<?> constructor,
                                 Class<?> implClass,
                                 InjectingComponentType componentType,
                                 IntrospectionContext context) {
    }

    public void visitConstructorParameter(A annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          InjectingComponentType componentType,
                                          IntrospectionContext context) {
    }
}
