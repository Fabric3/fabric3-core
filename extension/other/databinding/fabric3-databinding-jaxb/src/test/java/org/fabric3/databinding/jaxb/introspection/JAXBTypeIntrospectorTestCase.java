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

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.databinding.jaxb.mapper.JAXBQNameMapper;
import org.fabric3.databinding.jaxb.mapper.JAXBQNameMapperImpl;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.java.JavaType;

/**
 *
 */
public class JAXBTypeIntrospectorTestCase extends TestCase {
    private static final QName XSD_INT = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "int");
    private JAXBTypeIntrospector introspector;

    public void testDefaultMapping() throws Exception {
        Operation operation = createOperation("operation", int.class);
        introspector.introspect(operation, null, new DefaultIntrospectionContext());
        DataType dataType = operation.getInputTypes().get(0);
        assertEquals(XSD_INT, dataType.getXsdType());
    }

    protected void setUp() throws Exception {
        super.setUp();
        JAXBQNameMapper mapper = new JAXBQNameMapperImpl();
        introspector = new JAXBTypeIntrospector(mapper);
    }

    @SuppressWarnings({"unchecked"})
    private Operation createOperation(String name, Class<?> paramType) {
        JavaType type = new JavaType(paramType);
        List<DataType> in = new ArrayList<>();
        in.add(type);
        JavaType outputType = new JavaType(Void.class);
        return new Operation(name, in, outputType, null);
    }

    @XmlRootElement
    private class Param {

    }

}
