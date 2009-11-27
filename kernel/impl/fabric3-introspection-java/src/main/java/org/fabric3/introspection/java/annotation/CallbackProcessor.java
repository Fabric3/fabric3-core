/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.CallbackDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;

/**
 * @version $Rev$ $Date$
 */
public class CallbackProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Callback, I> {
    private final IntrospectionHelper helper;
    private final JavaContractProcessor contractProcessor;

    public CallbackProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(Callback.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void visitField(Callback annotation, Field field, Class<?> implClass, I implementation, IntrospectionContext context) {
        validate(field, context);

        String name = helper.getSiteName(field, null);
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        CallbackDefinition definition = createDefinition(name, type, implClass, context);
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(Callback annotation, Method method, Class<?> implClass, I implementation, IntrospectionContext context) {
        validate(method, context);

        String name = helper.getSiteName(method, null);
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        CallbackDefinition definition = createDefinition(name, type, implClass, context);
        implementation.getComponentType().add(definition, site);
    }

    private void validate(Field field, IntrospectionContext context) {
        if (!Modifier.isProtected(field.getModifiers()) && !Modifier.isPublic(field.getModifiers())) {
            Class<?> clazz = field.getDeclaringClass();
            InvalidAccessor warning =
                    new InvalidAccessor("Illegal callback. The field " + field.getName() + " on " + clazz.getName()
                            + " is annotated with @Callback and must be public or protected.", clazz);
            context.addError(warning);
        }
    }

    private void validate(Method method, IntrospectionContext context) {
        if (!Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers())) {
            Class<?> clazz = method.getDeclaringClass();
            InvalidAccessor warning = new InvalidAccessor("Illegal callback. The method " + method
                    + " is annotated with @Callback and must be public or protected.", clazz);
            context.addError(warning);
        }
    }

    private CallbackDefinition createDefinition(String name, Type type, Class<?> implClass, IntrospectionContext context) {
        TypeMapping mapping = context.getTypeMapping(implClass);
        Class<?> baseType = helper.getBaseType(type, mapping);
        ServiceContract contract = contractProcessor.introspect(baseType, implClass, context);
        return new CallbackDefinition(name, contract);
    }

}
