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
package org.fabric3.databinding.jaxb.format;

import junit.framework.TestCase;

/**
 * Note this test is commented out because some early versions of JDK 6 have an older version of the JAXB API.
 */
public class JAXBParameterEncoderFactoryTestCase extends TestCase {

    public void testSerializeDeserialize() throws Exception {
//        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
//        operation.addSourceParameterType(Foo.class.getName());
//        operation.addTargetParameterType(Foo.class.getName());
//        operation.setSourceReturnType(Void.class.getName());
//        operation.setTargetReturnType(Void.class.getName());
//
//        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
//        EasyMock.expect(chain.getPhysicalOperation()).andReturn(operation);
//        List<InvocationChain> chains = new ArrayList<InvocationChain>();
//        chains.add(chain);
//        Wire wire = EasyMock.createMock(Wire.class);
//        EasyMock.expect(wire.getInvocationChains()).andReturn(chains);
//        EasyMock.replay(chain, wire);
//
//        Message message = new MessageImpl();
//        message.setBody(new Object[]{new Foo()});
//        JAXBContextFactoryImpl jaxbFactory = new JAXBContextFactoryImpl();
//        JAXBParameterEncoderFactory factory = new JAXBParameterEncoderFactory(jaxbFactory);
//        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
//        String serialized = encoder.encodeText(message);
//        Object deserialized = encoder.decode("", serialized);
//        assertTrue(deserialized instanceof Foo);
    }

}
