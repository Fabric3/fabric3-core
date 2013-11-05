/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.spi.introspection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Base class for introspecting file binding information in a component implementation.
 */
@EagerInit
public abstract class AbstractBindingPostProcessor<A extends Annotation> implements PostProcessor {
    private Class<A> annotationType;
    private Method serviceAttribute;

    protected AbstractBindingPostProcessor(Class<A> annotationType) {
        this.annotationType = annotationType;
        try {
            serviceAttribute = annotationType.getDeclaredMethod("service");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Binding annotation must have a service attribute");
        }
    }

    public void process(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        processService(componentType, implClass, context);
        processReferences(componentType, implClass, context);
    }

    protected String getNullibleValue(String value) {
        return value.isEmpty() ? null : value;
    }

    protected abstract void processService(A annotation,
                                           AbstractService<?> service,
                                           InjectingComponentType componentType,
                                           Class<?> implClazz,
                                           IntrospectionContext context);

    protected abstract void processReference(A annotation,
                                             ReferenceDefinition reference,
                                             AccessibleObject object,
                                             Class<?> implClass,
                                             IntrospectionContext context);

    private void processService(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        A annotation = implClass.getAnnotation(annotationType);
        if (annotation == null) {
            return;
        }
        Class<?> serviceInterface = getService(annotation);
        if (serviceInterface.equals(Void.class)) {
            serviceInterface = null;
        }
        AbstractService boundService = null;
        if (serviceInterface == null) {
            if (componentType.getServices().size() != 1) {
                InvalidAnnotation error = new InvalidAnnotation("Binding annotation must specify a service interface", implClass, annotation, implClass);
                context.addError(error);
                return;
            }
            boundService = componentType.getServices().values().iterator().next();
        } else {
            String name = serviceInterface.getName();
            for (AbstractService service : componentType.getServices().values()) {
                String interfaceName = service.getServiceContract().getQualifiedInterfaceName();
                if (interfaceName.equals(name)) {
                    boundService = service;
                    break;
                }
            }
            if (boundService == null) {
                InvalidAnnotation error = new InvalidAnnotation("Service specified in binding annotation not found: " + name, implClass, annotation, implClass);
                context.addError(error);
                return;
            }
        }
        processService(annotation, boundService, componentType, implClass, context);
    }

    private void processReferences(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        for (Map.Entry<ModelObject, InjectionSite> entry : componentType.getInjectionSiteMappings().entrySet()) {
            if (!(entry.getKey() instanceof ReferenceDefinition)) {
                continue;
            }
            ReferenceDefinition reference = (ReferenceDefinition) entry.getKey();
            InjectionSite site = entry.getValue();
            if (site instanceof FieldInjectionSite) {
                FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                Field field = fieldSite.getField();
                processBindingAnnotation(field, reference, implClass, context);
            } else if (site instanceof MethodInjectionSite) {
                MethodInjectionSite methodSite = (MethodInjectionSite) site;
                Method method = methodSite.getMethod();
                processBindingAnnotation(method, reference, implClass, context);
            } else if (site instanceof ConstructorInjectionSite) {
                ConstructorInjectionSite constructorSite = (ConstructorInjectionSite) site;
                Constructor<?> constructor = constructorSite.getConstructor();
                Annotation[] annotations = constructor.getParameterAnnotations()[constructorSite.getParam()];
                for (Annotation annotation : annotations) {
                    if (annotationType.equals(annotation.annotationType())) {
                        processReference(annotationType.cast(annotation), reference, constructor, implClass, context);
                    }
                }
            }
        }

    }

    private void processBindingAnnotation(AccessibleObject object, ReferenceDefinition reference, Class<?> implClass, IntrospectionContext context) {
        A annotation = object.getAnnotation(annotationType);
        if (annotation == null) {
            return;
        }
        processReference(annotation, reference, object, implClass, context);

    }

    private Class<?> getService(A annotation) {
        try {
            return (Class<?>) serviceAttribute.invoke(annotation);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }

}
