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
package org.fabric3.resource.introspection;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.resource.model.SystemSourcedResourceReference;
import org.fabric3.resource.spi.JSR250ResourceTypeHandler;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes metadata for the {@link Resource} annotation.
 */
public class JSR250ResourceProcessor extends AbstractAnnotationProcessor<Resource> {
    private IntrospectionHelper helper;
    private JavaContractProcessor contractProcessor;
    private Map<Class<?>, JSR250ResourceTypeHandler> handlers = new HashMap<>();

    @Reference(required = false)
    public void setHandlers(Map<Class<?>, JSR250ResourceTypeHandler> handlers) {
        this.handlers = handlers;
    }

    public JSR250ResourceProcessor(@Reference IntrospectionHelper helper, @Reference JavaContractProcessor contractProcessor) {
        super(Resource.class);
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void visitField(Resource annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        String name = helper.getSiteName(field, annotation.name());
        Type genericType = field.getGenericType();
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        FieldInjectionSite site = new FieldInjectionSite(field);

        ResourceReference definition;
        JSR250ResourceTypeHandler handler = handlers.get(type);
        if (handler != null) {
            // there is a specific Handler for this type
            definition = handler.createResourceReference(name, annotation, field, context);
        } else {
            String mappedName = annotation.mappedName();
            if (mappedName.length() == 0) {
                // default to the field type simple name
                mappedName = type.getSimpleName();
            }
            definition = createResource(name, type, false, mappedName, componentType, context);
        }
        componentType.add(definition, site);
    }

    public void visitMethod(Resource annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {
        String name = helper.getSiteName(method, annotation.name());
        Type genericType = helper.getGenericType(method);
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);

        ResourceReference definition;
        JSR250ResourceTypeHandler handler = handlers.get(type);
        if (handler != null) {
            // there is a specific Handler for this type
            definition = handler.createResourceReference(name, annotation, method, context);
        } else {
            String mappedName = annotation.mappedName();
            if (mappedName.length() == 0) {
                // default to the field type simple name
                mappedName = type.getSimpleName();
            }
            definition = createResource(name, type, false, mappedName, componentType, context);
        }
        componentType.add(definition, site);
    }

    private SystemSourcedResourceReference createResource(String name,
                                                          Class<?> type,
                                                          boolean optional,
                                                          String mappedName,
                                                          InjectingComponentType componentType,
                                                          IntrospectionContext context) {
        ServiceContract serviceContract = contractProcessor.introspect(type, context, componentType);
        return new SystemSourcedResourceReference(name, optional, mappedName, serviceContract);
    }
}
