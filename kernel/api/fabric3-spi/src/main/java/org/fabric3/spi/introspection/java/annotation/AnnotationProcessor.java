/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.introspection.java.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 * Interface for processors that handle annotations attached to Java declarations.
 *
 * @version $Rev$ $Date$
 * @param <A> the type of annotation this processor handles
 */
public interface AnnotationProcessor<A extends Annotation, I extends Implementation<? extends InjectingComponentType>> {
    /**
     * Returns the type of annotation this processor handles.
     *
     * @return the type of annotation this processor handles
     */
    Class<A> getType();

    /**
     * Visit an annotation on a class or interface declaration.  If errors or warnings are encountered, they will be collated in the
     * IntrospectionContext.
     *
     * @param annotation     the annotation
     * @param type           the class or interface
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     */
    void visitType(A annotation, Class<?> type, I implementation, IntrospectionContext context);

    /**
     * Visit an annotation on a field declaration. If errors or warnings are encountered, they will be collated in the IntrospectionContext.
     *
     * @param annotation     the annotation
     * @param field          the field
     * @param implClass      the component implementation class. This may be different than the declaring field class.
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     */
    void visitField(A annotation, Field field, Class<?> implClass, I implementation, IntrospectionContext context);

    /**
     * Visit an annotation on a method declaration. If errors or warnings are encountered, they will be collated in the IntrospectionContext.
     *
     * @param annotation     the annotation
     * @param method         the method declaration
     * @param implClass      the component implementation class. This may be different than the declaring method class.
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     */
    void visitMethod(A annotation, Method method, Class<?> implClass, I implementation, IntrospectionContext context);

    /**
     * Visit an annotation on a method parameter declaration. If errors or warnings are encountered, they will be collated in the
     * IntrospectionContext.
     *
     * @param annotation     the annotation
     * @param method         the method declaration
     * @param index          the index of the method parameter
     * @param implClass      the component implementation class. This may be different than the declaring method class.
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     */
    void visitMethodParameter(A annotation, Method method, int index, Class<?> implClass, I implementation, IntrospectionContext context);

    /**
     * Visit an annotation on a constructor declaration. If errors or warnings are encountered, they will be collated in the IntrospectionContext.
     *
     * @param annotation     the annotation
     * @param constructor    the constructor
     * @param implClass      the component implementation class.
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     */
    void visitConstructor(A annotation, Constructor<?> constructor, Class<?> implClass, I implementation, IntrospectionContext context);

    /**
     * Visit an annotation on a constructor parameter declaration. If errors or warnings are encountered, they will be collated in the
     * IntrospectionContext.
     *
     * @param annotation     the annotation
     * @param constructor    the constructor
     * @param index          the index of the constructor parameter
     * @param implClass      the component implementation class.
     * @param implementation the implementation being introspected
     * @param context        the current introspection context
     */
    void visitConstructorParameter(A annotation,
                                   Constructor<?> constructor,
                                   int index,
                                   Class<?> implClass,
                                   I implementation,
                                   IntrospectionContext context);
}
