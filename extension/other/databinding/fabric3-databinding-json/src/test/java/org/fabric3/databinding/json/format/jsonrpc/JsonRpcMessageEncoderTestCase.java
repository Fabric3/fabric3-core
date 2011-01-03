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
package org.fabric3.databinding.json.format.jsonrpc;

import junit.framework.TestCase;

import org.fabric3.spi.invocation.Message;

/**
 * @version $Rev$ $Date$
 */
public class JsonRpcMessageEncoderTestCase extends TestCase {

//    public void testEncode() throws Exception {
//        // don't compare id
//        String expected = "{\"jsonrpc\":\"2.0\",\"params\":[\"one\",\"two\"],\"method\":\"test\",\"id\":\"";
//        JsonRpcMessageEncoder encoder = new JsonRpcMessageEncoder();
//        Message message = new MessageImpl();
//        message.setBody(new Object[]{"one", "two"});
//        String encoded = encoder.encodeText("test", message, null);
//        assertTrue(encoded.startsWith(expected));
//        assertTrue(encoded.endsWith("\"}"));
//    }
//
//    public void testEncodeComplexType() throws Exception {
//        // don't compare id
//        String expected = "{\"jsonrpc\":\"2.0\",\"params\":[{\"firstName\":\"first\",\"lastName\":\"last\"}],\"method\":\"test\",\"id\":\"";
//        JsonRpcMessageEncoder encoder = new JsonRpcMessageEncoder();
//        Message message = new MessageImpl();
//        Foo foo = new Foo();
//        foo.setFirstName("first");
//        foo.setLastName("last");
//        message.setBody(new Object[]{foo});
//        String encoded = encoder.encodeText("test", message, null);
//        assertTrue(encoded.startsWith(expected));
//        assertTrue(encoded.endsWith("\"}"));
//    }
//
//    public void testEncodeResponse() throws Exception {
//        String expected = "{\"jsonrpc\":\"2.0\",\"result\":\"response\",\"id\":\"\"}";
//        JsonRpcMessageEncoder encoder = new JsonRpcMessageEncoder();
//        Message message = new MessageImpl();
//        message.setBody("response");
//        String encoded = encoder.encodeResponseText("test", message, null);
//        assertEquals(expected, encoded);
//    }

    //
    public void testDecodeComplexType() throws Exception {
        String encoded = "{\"jsonrpc\":\"2.0\",\"params\":[{\"firstName\":\"first\",\"lastName\":\"last\"}],\"method\":\"test\",\"id\":\"1\"}";
        JsonRpcMessageEncoder encoder = new JsonRpcMessageEncoder();
        Message message = encoder.decode(encoded, null);
    }

    private class Foo {
        private String firstName;
        private String lastName;


        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }


}
