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

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Interface for processors that handle annotations attached to Java declarations.
 *
 * @param <A> the type of annotation this processor handles
 */
public interface AnnotationProcessor<A extends Annotation> {
    /**
     * Returns the type of annotation this processor handles.
     *
     * @return the type of annotation this processor handles
     */
    Class<A> getType();

    /**
     * Visit an annotation on a class or interface declaration.  If errors or warnings are encountered, they will be collated in the IntrospectionContext.
     *
     * @param annotation    the annotation
     * @param type          the class or interface
     * @param componentType the implementation component type being introspected
     * @param context       the current introspection context
     */
    void visitType(A annotation, Class<?> type, InjectingComponentType componentType, IntrospectionContext context);

    /**
     * Visit an annotation on a field declaration. If errors or warnings are encountered, they will be collated in the IntrospectionContext.
     *
     * @param annotation    the annotation
     * @param field         the field
     * @param implClass     the component implementation class. This may be different than the declaring field class.
     * @param componentType the implementation component type being introspected
     * @param context       the current introspection context
     */
    void visitField(A annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context);

    /**
     * Visit an annotation on a method declaration. If errors or warnings are encountered, they will be collated in the IntrospectionContext.
     *
     * @param annotation    the annotation
     * @param method        the method declaration
     * @param implClass     the component implementation class. This may be different than the declaring method class.
     * @param componentType the implementation component type being introspected
     * @param context       the current introspection context
     */
    void visitMethod(A annotation, Method method, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context);

    /**
     * Visit an annotation on a method parameter declaration. If errors or warnings are encountered, they will be collated in the IntrospectionContext.
     *
     * @param annotation    the annotation
     * @param method        the method declaration
     * @param index         the index of the method parameter
     * @param implClass     the component implementation class. This may be different than the declaring method class.
     * @param componentType the implementation component type being introspected
     * @param context       the current introspection context
     */
    void visitMethodParameter(A annotation, Method method, int index, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context);

    /**
     * Visit an annotation on a constructor declaration. If errors or warnings are encountered, they will be collated in the IntrospectionContext.
     *
     * @param annotation    the annotation
     * @param constructor   the constructor
     * @param implClass     the component implementation class.
     * @param componentType the implementation component type being introspected
     * @param context       the current introspection context
     */
    void visitConstructor(A annotation, Constructor<?> constructor, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context);

    /**
     * Visit an annotation on a constructor parameter declaration. If errors or warnings are encountered, they will be collated in the IntrospectionContext.
     *
     * @param annotation    the annotation
     * @param constructor   the constructor
     * @param index         the index of the constructor parameter
     * @param implClass     the component implementation class.
     * @param componentType the implementation component type being introspected
     * @param context       the current introspection context
     */
    void visitConstructorParameter(A annotation,
                                   Constructor<?> constructor,
                                   int index,
                                   Class<?> implClass,
                                   InjectingComponentType componentType,
                                   IntrospectionContext context);
}
