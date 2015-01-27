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
 */
package org.fabric3.fabric.container.builder.transform;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.w3c.dom.Document;

import org.fabric3.fabric.container.interceptor.TransformerInterceptorFactoryImpl;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.physical.PhysicalDataTypes;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 *
 */
public class TransformerInterceptorFactoryImplTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testCreateInterceptor() throws Exception {
        ClassLoader loader = getClass().getClassLoader();

        List<DataType> sourceTypes = new ArrayList<>();
        sourceTypes.add(PhysicalDataTypes.JAVA_TYPE);
        List<DataType> targetTypes = new ArrayList<>();
        targetTypes.add(PhysicalDataTypes.JAXB);

        Transformer in = EasyMock.createMock(Transformer.class);
        Transformer out = EasyMock.createMock(Transformer.class);

        TransformerRegistry registry = EasyMock.createMock(TransformerRegistry.class);
        EasyMock.expect(registry.getTransformer(EasyMock.eq(PhysicalDataTypes.JAVA_TYPE),
                                                EasyMock.eq(PhysicalDataTypes.JAXB),
                                                EasyMock.isA(List.class),
                                                EasyMock.isA(List.class))).andReturn(in);
        EasyMock.expect(registry.getTransformer(EasyMock.eq(PhysicalDataTypes.JAXB),
                                                EasyMock.eq(PhysicalDataTypes.JAVA_TYPE),
                                                EasyMock.isA(List.class),
                                                EasyMock.isA(List.class))).andReturn(out);
        EasyMock.replay(registry, in, out);

        TransformerInterceptorFactoryImpl factory = new TransformerInterceptorFactoryImpl(registry);

        PhysicalOperationDefinition definition = new PhysicalOperationDefinition();
        definition.addSourceParameterType("java.lang.String");
        definition.setSourceReturnType("java.lang.String");
        definition.addTargetParameterType(Document.class.getName());
        definition.setTargetReturnType(Document.class.getName());

        assertNotNull(factory.createInterceptor(definition, sourceTypes, targetTypes, loader, loader));

        EasyMock.verify(registry, in, out);
    }
}
