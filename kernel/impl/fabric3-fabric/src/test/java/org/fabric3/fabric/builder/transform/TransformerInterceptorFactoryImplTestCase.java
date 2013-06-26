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
package org.fabric3.fabric.builder.transform;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.w3c.dom.Document;

import org.fabric3.fabric.interceptor.TransformerInterceptorFactoryImpl;
import org.fabric3.model.type.contract.DataType;
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

        List<DataType<?>> sourceTypes = new ArrayList<DataType<?>>();
        sourceTypes.add(PhysicalDataTypes.JAVA_TYPE);
        List<DataType<?>> targetTypes = new ArrayList<DataType<?>>();
        targetTypes.add(PhysicalDataTypes.DOM);

        Transformer in = EasyMock.createMock(Transformer.class);
        Transformer out = EasyMock.createMock(Transformer.class);

        TransformerRegistry registry = EasyMock.createMock(TransformerRegistry.class);
        EasyMock.expect(registry.getTransformer(EasyMock.eq(PhysicalDataTypes.JAVA_TYPE),
                                                EasyMock.eq(PhysicalDataTypes.DOM),
                                                EasyMock.isA(List.class),
                                                EasyMock.isA(List.class))).andReturn(in);
        EasyMock.expect(registry.getTransformer(EasyMock.eq(PhysicalDataTypes.DOM),
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
