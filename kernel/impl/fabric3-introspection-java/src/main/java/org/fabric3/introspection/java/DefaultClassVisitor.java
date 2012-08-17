/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.introspection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.AnnotationProcessor;
import org.fabric3.spi.introspection.java.annotation.ClassVisitor;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 * Default ClassVisitor implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultClassVisitor implements ClassVisitor {

    private Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation>> processors;
    private PolicyAnnotationProcessor policyProcessor;

    /**
     * Constructor used from the bootstrapper.
     *
     * @param processors the generic annotation processors
     */
    public DefaultClassVisitor(Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation>> processors) {
        this.processors = processors;
    }

    /**
     * Constructor.
     */
    @org.oasisopen.sca.annotation.Constructor
    public DefaultClassVisitor() {
    }

    @Reference
    public void setProcessors(Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation>> processors) {
        this.processors = processors;
    }

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
    }

    public void visit(InjectingComponentType componentType, Class<?> clazz, IntrospectionContext context) {
        walk(componentType, clazz, clazz, false, context);
    }

    private void walk(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, boolean isSuperClass, IntrospectionContext context) {
        if (!clazz.isInterface()) {
            walkSuperClasses(componentType, clazz, implClass, context);
        }

        walkInterfaces(componentType, clazz, implClass, context);

        walkClass(componentType, clazz, context);

        walkFields(componentType, clazz, implClass, context);

        walkMethods(componentType, clazz, implClass, context);

        if (!isSuperClass) {
            // If a super class is being evaluated, ignore its constructors.
            // Otherwise references, properties, or resources may be incorrectly introspected.
            walkConstructors(componentType, clazz, implClass, context);
        }
    }

    private void walkSuperClasses(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            walk(componentType, superClass, implClass, true, context);
        }
    }

    private void walkInterfaces(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
        for (Class<?> interfaze : clazz.getInterfaces()) {
            walk(componentType, interfaze, implClass, false, context);
        }
    }

    private void walkClass(InjectingComponentType componentType, Class<?> clazz, IntrospectionContext context) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            visitType(annotation, clazz, componentType, context);
        }
    }

    private void walkFields(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                visitField(annotation, field, implClass, componentType, context);
            }
        }
    }

    private void walkMethods(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                visitMethod(annotation, method, implClass, componentType, context);
            }

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                for (Annotation annotation : annotations) {
                    visitMethodParameter(annotation, method, i, implClass, componentType, context);
                }
            }
        }
    }

    private void walkConstructors(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            for (Annotation annotation : constructor.getDeclaredAnnotations()) {
                visitConstructor(annotation, constructor, implClass, componentType, context);
            }

            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                for (Annotation annotation : annotations) {
                    visitConstructorParameter(annotation, constructor, i, implClass, componentType, context);
                }
            }
        }
    }

    private <A extends Annotation> void visitType(A annotation, Class<?> clazz, InjectingComponentType componentType, IntrospectionContext context) {
        AnnotationProcessor<A> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitType(annotation, clazz, componentType, context);
        } else {
            // check if the annotation is a policy set or intent
            if (policyProcessor != null) {
                policyProcessor.process(annotation, componentType, context);
            }
        }
    }

    private <A extends Annotation> void visitField(A annotation,
                                                   Field field,
                                                   Class<?> implClass,
                                                   InjectingComponentType componentType,
                                                   IntrospectionContext context) {
        AnnotationProcessor<A> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitField(annotation, field, implClass, componentType, context);
        }
    }

    private <A extends Annotation> void visitMethod(A annotation,
                                                    Method method,
                                                    Class<?> implClass,
                                                    InjectingComponentType componentType,
                                                    IntrospectionContext context) {
        AnnotationProcessor<A> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitMethod(annotation, method, implClass, componentType, context);
        }
    }

    private <A extends Annotation> void visitMethodParameter(A annotation,
                                                             Method method,
                                                             int index,
                                                             Class<?> implClass,
                                                             InjectingComponentType componentType,
                                                             IntrospectionContext context) {
        AnnotationProcessor<A> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitMethodParameter(annotation, method, index, implClass, componentType, context);
        }
    }

    private <A extends Annotation> void visitConstructor(A annotation,
                                                         Constructor<?> constructor,
                                                         Class<?> implClass,
                                                         InjectingComponentType componentType,
                                                         IntrospectionContext context) {
        AnnotationProcessor<A> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitConstructor(annotation, constructor, implClass, componentType, context);
        }
    }

    private <A extends Annotation> void visitConstructorParameter(A annotation,
                                                                  Constructor<?> constructor,
                                                                  int index,
                                                                  Class<?> implClass,
                                                                  InjectingComponentType componentType,
                                                                  IntrospectionContext context) {
        AnnotationProcessor<A> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitConstructorParameter(annotation, constructor, index, implClass, componentType, context);
        }
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> AnnotationProcessor<A> getProcessor(A annotation) {
        return (AnnotationProcessor<A>) processors.get(annotation.annotationType());
    }

}
