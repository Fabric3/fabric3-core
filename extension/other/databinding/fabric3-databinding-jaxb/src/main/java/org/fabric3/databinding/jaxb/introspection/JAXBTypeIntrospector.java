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
package org.fabric3.databinding.jaxb.introspection;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.TypeIntrospector;
import org.fabric3.spi.model.type.java.JavaType;

/**
 * Introspects operations for the presence of JAXB types.
 */
public class JAXBTypeIntrospector implements TypeIntrospector {
    private static final String JAXB = "JAXB";
    private static final String DEFAULT = "##default";

    public void introspect(Operation operation, Method method, IntrospectionContext context) {
        // TODO perform error checking, e.g. mixing of databindings
        List<DataType> inputTypes = operation.getInputTypes();
        for (DataType type : inputTypes) {
            if (!(type instanceof JavaType)) {
                // programming error
                throw new AssertionError("Java contracts must use " + JavaType.class);
            }
            introspect(type);
        }
        for (DataType type : operation.getFaultTypes()) {
            // FIXME need to process fault beans
            if (!(type instanceof JavaType)) {
                // programming error
                throw new AssertionError("Java contracts must use " + JavaType.class);
            }
            introspect(type);
        }
        DataType outputType = operation.getOutputType();
        if (!(outputType instanceof JavaType)) {
            // programming error
            throw new AssertionError("Java contracts must use " + JavaType.class);
        }
        introspect(outputType);

    }

    public void introspect(DataType dataType) {
        Class<?> type = dataType.getType();
        XmlRootElement annotation = type.getAnnotation(XmlRootElement.class);
        if (annotation != null) {
            String namespace = annotation.namespace();
            if (DEFAULT.equals(namespace)) {
                namespace = getDefaultNamespace(type);
            }
            dataType.setDatabinding(JAXB);
            return;
        }
        XmlType typeAnnotation = type.getAnnotation(XmlType.class);
        if (typeAnnotation != null) {
            String namespace = typeAnnotation.namespace();
            if (DEFAULT.equals(namespace)) {
                namespace = getDefaultNamespace(type);
            }
            String name = typeAnnotation.name();
            if (DEFAULT.equals(namespace)) {
                // as per the JAXB specification
                name = Introspector.decapitalize(type.getSimpleName());
            }
            dataType.setDatabinding(JAXB);
        }
    }

    private String getDefaultNamespace(Class clazz) {
        Package pkg = clazz.getPackage();
        // as per the JAXB specification
        if (pkg != null) {
            XmlSchema schemaAnnotation = pkg.getAnnotation(XmlSchema.class);
            if (schemaAnnotation != null) {
                return schemaAnnotation.namespace();
            }
            return pkg.getName();
        }
        return "";
    }
}
