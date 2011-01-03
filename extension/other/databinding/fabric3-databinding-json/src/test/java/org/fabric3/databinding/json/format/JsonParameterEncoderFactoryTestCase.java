/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.databinding.json.format;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class JsonParameterEncoderFactoryTestCase extends TestCase {

    public void testSerializeDeserializeString() throws Exception {
        JsonParameterEncoderFactory factory = new JsonParameterEncoderFactory();

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName("test");
        operation.addSourceParameterType(String.class.getName());
        operation.addTargetParameterType(String.class.getName());
        operation.setSourceReturnType(Void.class.getName());
        operation.setTargetReturnType(Void.class.getName());

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(operation);
        List<InvocationChain> chains = new ArrayList<InvocationChain>();
        chains.add(chain);
        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getInvocationChains()).andReturn(chains);
        EasyMock.replay(chain, wire);

        Message message = new MessageImpl();
        message.setBody(new Object[]{"test"});

        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
        String serialized = encoder.encodeText(message);
        assertEquals("test", encoder.decode("test", serialized));
    }

    public void testSerializeDeserializeNull() throws Exception {
        JsonParameterEncoderFactory factory = new JsonParameterEncoderFactory();

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName("test");
        operation.setSourceReturnType(Void.class.getName());
        operation.setTargetReturnType(Void.class.getName());

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(operation);
        List<InvocationChain> chains = new ArrayList<InvocationChain>();
        chains.add(chain);
        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getInvocationChains()).andReturn(chains);
        EasyMock.replay(chain, wire);

        Message message = new MessageImpl();

        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
        String serialized = encoder.encodeText(message);
        assertNull(encoder.decode("test", serialized));
    }

    public void testSerializeDeserializeObject() throws Exception {
        JsonParameterEncoderFactory factory = new JsonParameterEncoderFactory();

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName("test");
        operation.addSourceParameterType(Foo.class.getName());
        operation.addTargetParameterType(Foo.class.getName());
        operation.setSourceReturnType(Void.class.getName());
        operation.setTargetReturnType(Void.class.getName());

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(operation);
        List<InvocationChain> chains = new ArrayList<InvocationChain>();
        chains.add(chain);
        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getInvocationChains()).andReturn(chains);
        EasyMock.replay(chain, wire);

        Message message = new MessageImpl();
        message.setBody(new Object[]{new Foo()});

        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
        String serialized = encoder.encodeText(message);
        assertTrue(encoder.decode("test", serialized) instanceof Foo);
    }

    public void testSerializeDeserializeException() throws Exception {
        JsonParameterEncoderFactory factory = new JsonParameterEncoderFactory();

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName("test");
        operation.addSourceFaultType(FooException.class.getName());
        operation.addTargetFaultType(FooException.class.getName());
        operation.setSourceReturnType(Void.class.getName());
        operation.setTargetReturnType(Void.class.getName());

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(operation);
        List<InvocationChain> chains = new ArrayList<InvocationChain>();
        chains.add(chain);
        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getInvocationChains()).andReturn(chains);
        EasyMock.replay(chain, wire);

        Message message = new MessageImpl();
        FooException fault = new FooException("test");
        message.setBodyWithFault(fault);

        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
        String serialized = encoder.encodeText(message);
        FooException e = (FooException) encoder.decodeFault("test", serialized);
        assertEquals("test", e.getMessage());

    }


    private static class Foo {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class FooException extends Exception {
        private static final long serialVersionUID = 4937174167807498685L;

        public FooException(String message) {
            super(message);
        }
    }
}
