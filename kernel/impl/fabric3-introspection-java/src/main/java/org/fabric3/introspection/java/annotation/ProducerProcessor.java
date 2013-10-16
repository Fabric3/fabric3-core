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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.java.annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.fabric3.api.annotation.Producer;
import org.fabric3.model.type.component.ProducerDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Introspects {@link Producer} annotations.
 */
@EagerInit
public class ProducerProcessor extends AbstractAnnotationProcessor<Producer> {
    private JavaContractProcessor contractProcessor;
    private IntrospectionHelper helper;

    public ProducerProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(Producer.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void visitField(Producer annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        String name = helper.getSiteName(field, annotation.value());
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        ProducerDefinition definition = createDefinition(name, type, implClass, componentType, field, context);
        componentType.add(definition, site);
    }

    public void visitMethod(Producer annotation, Method method, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {

        String name = helper.getSiteName(method, annotation.value());
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        ProducerDefinition definition = createDefinition(name, type, implClass, componentType, method, context);
        componentType.add(definition, site);
    }

    public void visitConstructorParameter(Producer annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          InjectingComponentType componentType,
                                          IntrospectionContext context) {

        String name = helper.getSiteName(constructor, index, annotation.value());
        Type type = helper.getGenericType(constructor, index);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        ProducerDefinition definition = createDefinition(name, type, implClass, componentType, constructor, context);
        componentType.add(definition, site);
    }

    protected ProducerDefinition createDefinition(String name,
                                                  Type type,
                                                  Class<?> implClass,
                                                  InjectingComponentType componentType,
                                                  Member member,
                                                  IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> baseType = helper.getBaseType(type, typeMapping);
        ServiceContract contract = contractProcessor.introspect(baseType, implClass, context, componentType);
        if (contract.getOperations().size() != 1) {
            String interfaceName = contract.getInterfaceName();
            InvalidProducerInterface error = new InvalidProducerInterface("Producer interfaces must have one method: " + interfaceName, member, componentType);
            context.addError(error);
        }
        // TODO handle policies
        return new ProducerDefinition(name, contract);
    }

}