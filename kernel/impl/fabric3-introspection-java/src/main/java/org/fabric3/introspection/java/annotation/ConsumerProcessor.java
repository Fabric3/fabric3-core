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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.java.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.Consumer;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.fabric3.spi.model.type.java.Signature;

/**
 * Introspects {@link Consumer} annotations.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ConsumerProcessor extends AbstractAnnotationProcessor<Consumer> {
    private IntrospectionHelper helper;

    public ConsumerProcessor(@Reference IntrospectionHelper helper) {
        super(Consumer.class);
        this.helper = helper;
    }

    public void visitMethod(Consumer annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {
        if (method.getParameterTypes().length > 1) {
            InvalidConsumerMethod failure = new InvalidConsumerMethod("Consumer method " + method + " has more than one parameter");
            context.addError(failure);
            return;
        }
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        List<DataType<?>> types = introspectParameterTypes(method, typeMapping);
        // TODO handle policies
        String name = helper.getSiteName(method, annotation.value());
        Signature signature = new Signature(method);
        ConsumerDefinition definition = new ConsumerDefinition(name, types);
        componentType.add(definition, signature);
    }

    private List<DataType<?>> introspectParameterTypes(Method method, TypeMapping typeMapping) {
        Class<?>[] physicalParameterTypes = method.getParameterTypes();
        Type[] gParamTypes = method.getGenericParameterTypes();
        List<DataType<?>> parameterDataTypes = new ArrayList<DataType<?>>(gParamTypes.length);
        for (int i = 0; i < gParamTypes.length; i++) {
            Type gParamType = gParamTypes[i];
            Type logicalParamType = typeMapping.getActualType(gParamType);
            DataType<?> dataType = createDataType(physicalParameterTypes[i], logicalParamType, typeMapping);
            parameterDataTypes.add(dataType);
        }
        return parameterDataTypes;
    }

    @SuppressWarnings({"unchecked"})
    private DataType<?> createDataType(Class<?> physicalType, Type type, TypeMapping mapping) {
        if (type instanceof Class) {
            // not a generic
            return new JavaClass(physicalType);
        } else {
            JavaTypeInfo info = helper.createTypeInfo(type, mapping);
            return new JavaGenericType(info);
        }
    }

}