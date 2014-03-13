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
