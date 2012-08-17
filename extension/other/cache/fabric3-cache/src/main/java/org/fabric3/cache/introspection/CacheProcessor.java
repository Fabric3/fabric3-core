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
 *
 */
package org.fabric3.cache.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.Cache;
import org.fabric3.cache.model.CacheReferenceDefinition;
import org.fabric3.cache.spi.MissingCacheName;
import org.fabric3.model.type.component.ResourceReferenceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 * Introspects fields, methods and constructor parameters annotated with {@link Cache}.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CacheProcessor extends AbstractAnnotationProcessor<Cache> {
    private JavaContractProcessor contractProcessor;
    private IntrospectionHelper helper;

    public CacheProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(Cache.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void visitField(Cache annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        String name = helper.getSiteName(field, null);
        FieldInjectionSite site = new FieldInjectionSite(field);
        Class<?> type = field.getType();
        ResourceReferenceDefinition definition = create(name, annotation, type, field, context);
        componentType.add(definition, site);
    }

    public void visitMethod(Cache annotation, Method method, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        if (method.getParameterTypes().length != 1) {
            InvalidCacheSetter error = new InvalidCacheSetter("Setter must contain one parameter: " + method);
            context.addError(error);
            return;
        }
        String name = helper.getSiteName(method, null);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Class<?> type = method.getParameterTypes()[0];
        ResourceReferenceDefinition definition = create(name, annotation, type, method, context);
        componentType.add(definition, site);
    }

    public void visitConstructorParameter(Cache annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          InjectingComponentType componentType,
                                          IntrospectionContext context) {
        String name = annotation.name();
        Class<?> type = constructor.getParameterTypes()[index];
        ResourceReferenceDefinition definition = create(name, annotation, type, constructor, context);
        componentType.add(definition);
    }

    private ResourceReferenceDefinition create(String name, Cache annotation, Class<?> type, Member member, IntrospectionContext context) {
        ServiceContract contract = contractProcessor.introspect(type, context);
        String cacheName = annotation.name();
        if (cacheName.length() == 0) {
            MissingCacheName error = new MissingCacheName(member.getDeclaringClass());
            context.addError(error);
            return new CacheReferenceDefinition(name, contract, false, "error");
        }
        return new CacheReferenceDefinition(name, contract, false, cacheName);
    }


}
