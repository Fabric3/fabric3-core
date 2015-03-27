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
package org.fabric3.channel.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabric3.api.ChannelContext;
import org.fabric3.api.annotation.Channel;
import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.channel.model.ChannelResourceReference;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes {@link Channel} annotations.
 */
@EagerInit
@Key("org.fabric3.api.annotation.Channel")
public class ChannelAnnotationProcessor extends AbstractAnnotationProcessor<Channel> {
    private ServiceContract contextContract;
    private IntrospectionHelper helper;

    public ChannelAnnotationProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(Channel.class);
        this.helper = helper;
        IntrospectionContext context = new DefaultIntrospectionContext();
        contextContract = contractProcessor.introspect(ChannelContext.class, context);
        assert !context.hasErrors(); // should not happen
    }

    public void visitConstructorParameter(Channel annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          InjectingComponentType componentType,
                                          IntrospectionContext context) {
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        String name = helper.getSiteName(constructor, index, null);
        Class<?> type = constructor.getParameterTypes()[index];
        createReference(name, annotation, type, componentType, site, implClass, context);

    }

    public void visitField(Channel annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        FieldInjectionSite site = new FieldInjectionSite(field);
        String name = helper.getSiteName(field, null);
        Class<?> type = field.getType();
        createReference(name, annotation, type, componentType, site, implClass, context);
    }

    public void visitMethod(Channel annotation, Method method, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        String name = helper.getSiteName(method, null);
        Class<?> type = method.getParameterTypes()[0];
        createReference(name, annotation, type, componentType, site, implClass, context);
    }

    private void createReference(String name,
                                 Channel annotation,
                                 Class<?> type,
                                 InjectingComponentType componentType,
                                 InjectionSite site,
                                 Class<?> implClass,
                                 IntrospectionContext context) {
        if (ChannelContext.class.equals(type)) {
            String channelName = annotation.value();
            ChannelResourceReference reference = new ChannelResourceReference(name, channelName, contextContract);
            componentType.add(reference, site);
        } else {
            String message = "Invalid channel context type " + type.getName() + " on " + implClass.getName();
            InvalidChannelContextType error = new InvalidChannelContextType(message, null, componentType);
            context.addError(error);
        }
    }

}