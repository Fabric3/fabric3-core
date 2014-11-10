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
package org.fabric3.introspection.java.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.oasisopen.sca.ComponentContext;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 * Processes {@link Context} annotations.
 */
public class OASISContextProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Context> {
    private final IntrospectionHelper helper;

    public OASISContextProcessor(@Reference IntrospectionHelper helper) {
        super(Context.class);
        this.helper = helper;
    }

    public void visitField(Context annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        visit(type, componentType, site, field.getDeclaringClass(), field, context);
    }

    public void visitMethod(Context annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        visit(type, componentType, site, method.getDeclaringClass(), method, context);
    }

    private void visit(Type type,
                       InjectingComponentType componentType,
                       InjectionSite site,
                       Class<?> clazz,
                       Member member,
                       IntrospectionContext context) {
        if (!(type instanceof Class)) {
            context.addError(new InvalidContextType("Context type " + type + " is not supported in " + clazz.getName(), member, componentType));
        } else if (RequestContext.class.isAssignableFrom((Class<?>) type)) {
            componentType.addInjectionSite(site, Injectable.OASIS_REQUEST_CONTEXT);
        } else if (ComponentContext.class.isAssignableFrom((Class<?>) type)) {
            componentType.addInjectionSite(site, Injectable.OASIS_COMPONENT_CONTEXT);
        } else {
            context.addError(new InvalidContextType("Context type is not supported: " + type, member, componentType));
        }
    }
}