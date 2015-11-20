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
package org.fabric3.binding.rs.introspection;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.binding.rs.model.RsContextResourceReference;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes {@link Context} annotations.
 */
@EagerInit
@Key("javax.ws.rs.core.Context")
public class RsContextProcessor extends AbstractAnnotationProcessor<Context> {
    private IntrospectionHelper helper;

    public RsContextProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(Context.class);
        this.helper = helper;
    }

    public void visitField(Context annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        FieldInjectionSite site = new FieldInjectionSite(field);
        String name = helper.getSiteName(field, null);
        Class<?> type = field.getType();
        if (!validateType(type, field, annotation, implClass, context)) {
            return;
        }
        RsContextResourceReference reference = new RsContextResourceReference(name, type);
        componentType.add(reference, site);
    }

    public void visitMethod(Context annotation, Method method, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        Type[] types = method.getGenericParameterTypes();
        if (types.length != 1) {
            InvalidAnnotation error = new InvalidAnnotation("Context injection methods must have a single parameter", method, annotation, implClass);
            context.addError(error);
            return;
        }
        Class<?> type = (Class<?>) types[0];
        if (!validateType(type, method, annotation, implClass, context)) {
            return;
        }

        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        String name = helper.getSiteName(method, null);

        RsContextResourceReference reference = new RsContextResourceReference(name, type);
        componentType.add(reference, site);

    }

    public void visitConstructorParameter(Context annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          InjectingComponentType componentType,
                                          IntrospectionContext context) {
        Class<?> type = constructor.getParameterTypes()[index];
        if (!validateType(type, constructor, annotation, implClass, context)) {
            return;
        }
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, 0);
        String name = helper.getSiteName(constructor, index, null);

        RsContextResourceReference reference = new RsContextResourceReference(name, type);
        componentType.add(reference, site);

    }

    private boolean validateType(Class<?> type, AnnotatedElement element, Annotation annotation, Class<?> clazz, IntrospectionContext context) {
        if (ContainerRequestContext.class.isAssignableFrom(type) || Application.class.isAssignableFrom(type) || UriInfo.class.isAssignableFrom(type)
            || Request.class.isAssignableFrom(type) || HttpHeaders.class.isAssignableFrom(type) || SecurityContext.class.isAssignableFrom(type)) {
            return true;
        } else {
            InvalidAnnotation error = new InvalidAnnotation("Unsupported context type", element, annotation, clazz);
            context.addError(error);
            return false;
        }
    }
}