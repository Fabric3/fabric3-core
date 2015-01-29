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
package org.fabric3.introspection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.api.annotation.model.Binding;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.AnnotationProcessor;
import org.fabric3.spi.introspection.java.annotation.ClassVisitor;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default ClassVisitor implementation.
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
        visit(componentType, clazz, clazz, false, context);
    }

    private void visit(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, boolean isSuperClass, IntrospectionContext context) {
        if (!clazz.isInterface()) {
            visitSuperClasses(componentType, clazz, implClass, context);
        }

        visitInterfaces(componentType, clazz, implClass, context);

        visitClass(componentType, clazz, context);

        visitFields(componentType, clazz, implClass, context);

        visitMethods(componentType, clazz, implClass, context);

        if (!isSuperClass) {
            // If a super class is being evaluated, ignore its constructors.
            // Otherwise references, properties, or resources may be incorrectly introspected.
            visitConstructors(componentType, clazz, implClass, context);
        }
    }

    private void visitSuperClasses(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            visit(componentType, superClass, implClass, true, context);
        }
    }

    private void visitInterfaces(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
        for (Class<?> interfaze : clazz.getInterfaces()) {
            visit(componentType, interfaze, implClass, false, context);
        }
    }

    private void visitClass(InjectingComponentType componentType, Class<?> clazz, IntrospectionContext context) {
        for (Annotation annotation : clazz.getDeclaredAnnotations()) {
            visitType(annotation, clazz, componentType, context);
        }
    }

    private void visitFields(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
        for (Field field : clazz.getDeclaredFields()) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                visitField(annotation, field, implClass, componentType, context);
            }
            for (Annotation annotation : annotations) {
                Binding binding = annotation.annotationType().getAnnotation(Binding.class);
                if (binding != null) {
                    visitField(binding, field, implClass, componentType, context);
                }
            }
        }
    }

    private void visitMethods(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
        for (Method method : clazz.getDeclaredMethods()) {
            Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
            for (Annotation annotation : declaredAnnotations) {
                visitMethod(annotation, method, implClass, componentType, context);
            }
            for (Annotation annotation : declaredAnnotations) {
                Binding binding = annotation.annotationType().getAnnotation(Binding.class);
                if (binding != null) {
                    visitMethod(binding, method, implClass, componentType, context);
                }
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

    private void visitConstructors(InjectingComponentType componentType, Class<?> clazz, Class<?> implClass, IntrospectionContext context) {
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
                for (Annotation annotation : annotations) {
                    Binding binding = annotation.annotationType().getAnnotation(Binding.class);
                    if (binding != null) {
                        visitConstructorParameter(binding, constructor, i, implClass, componentType, context);
                    }
                }
            }
        }
    }

    private <A extends Annotation> void visitType(A annotation, Class<?> clazz, InjectingComponentType componentType, IntrospectionContext context) {
        AnnotationProcessor<A> processor = getProcessor(annotation);
        if (processor != null) {
            processor.visitType(annotation, clazz, componentType, context);
        } else {
            // check if the annotation is a policy
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
        } else {
            // check if the annotation is a policy
            if (policyProcessor != null) {
                policyProcessor.process(annotation, componentType, context);
            }
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
