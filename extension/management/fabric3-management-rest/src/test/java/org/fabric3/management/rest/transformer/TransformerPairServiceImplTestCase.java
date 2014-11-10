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
package org.fabric3.management.rest.transformer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 *
 */
public class TransformerPairServiceImplTestCase extends TestCase {
    private static final JavaType JAVA_TYPE = new JavaType(Object.class);

    private List<Method> methods;

    @SuppressWarnings({"unchecked"})
    public void testGetTransformerPairMultipleMethods() throws Exception {
        Transformer transformer = EasyMock.createMock(Transformer.class);
        TransformerRegistry registry = EasyMock.createMock(TransformerRegistry.class);
        registry.getTransformer(EasyMock.isA(JavaType.class), EasyMock.isA(JavaType.class), EasyMock.isA(List.class), EasyMock.isA(List.class));
        EasyMock.expectLastCall().andReturn(transformer).times(2);

        EasyMock.replay(transformer, registry);

        TransformerPairServiceImpl service = new TransformerPairServiceImpl(registry);

        service.getTransformerPair(methods, JAVA_TYPE, JAVA_TYPE);

        EasyMock.verify(transformer, registry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGetTransformerPairSingleMethods() throws Exception {
        Transformer transformer = EasyMock.createMock(Transformer.class);
        TransformerRegistry registry = EasyMock.createMock(TransformerRegistry.class);
        registry.getTransformer(EasyMock.isA(JavaType.class), EasyMock.isA(JavaType.class), EasyMock.isA(List.class), EasyMock.isA(List.class));
        EasyMock.expectLastCall().andReturn(transformer).times(2);

        EasyMock.replay(transformer, registry);

        TransformerPairServiceImpl service = new TransformerPairServiceImpl(registry);

        Method method = getClass().getDeclaredMethod("method1", String.class);
        service.getTransformerPair(Collections.singletonList(method), JAVA_TYPE, JAVA_TYPE);

        EasyMock.verify(transformer, registry);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        methods = new ArrayList<>();
        methods.add(getClass().getDeclaredMethod("method1", String.class));
        methods.add(getClass().getDeclaredMethod("method2", Integer.TYPE));
        methods.add(getClass().getDeclaredMethod("method3"));
    }

    private String method1(String param) {
        return param;
    }

    private void method2(int param) {

    }

    private void method3() {

    }


}
