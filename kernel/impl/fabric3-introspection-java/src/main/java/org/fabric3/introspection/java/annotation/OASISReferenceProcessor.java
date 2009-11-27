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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;

/**
 * @version $Rev$ $Date$
 */
public class OASISReferenceProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractReferenceProcessor<Reference, I> {

    public OASISReferenceProcessor(@org.osoa.sca.annotations.Reference JavaContractProcessor contractProcessor,
                                   @org.osoa.sca.annotations.Reference IntrospectionHelper helper) {
        super(Reference.class, contractProcessor, helper);
    }

    public void visitField(Reference annotation, Field field, Class<?> implClass, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(field, annotation.name());
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        Annotation[] annotations = field.getAnnotations();
        boolean required = annotation.required();
        ReferenceDefinition definition = createDefinition(name, required, type, implClass, annotations, context);
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(Reference annotation, Method method, Class<?> implClass, I implementation, IntrospectionContext context) {

        String name = helper.getSiteName(method, annotation.name());
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Annotation[] annotations = method.getAnnotations();
        ReferenceDefinition definition = createDefinition(name, annotation.required(), type, implClass, annotations, context);
        implementation.getComponentType().add(definition, site);
    }

    public void visitConstructorParameter(Reference annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass, I implementation,
                                          IntrospectionContext context) {

        String name = helper.getSiteName(constructor, index, annotation.name());
        Type type = helper.getGenericType(constructor, index);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        Annotation[] annotations = constructor.getParameterAnnotations()[index];
        boolean required = annotation.required();
        ReferenceDefinition definition = createDefinition(name, required, type, implClass, annotations, context);
        implementation.getComponentType().add(definition, site);
    }


}