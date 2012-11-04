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
*/
package org.fabric3.management.rest.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.management.rest.Constants;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.transform.Transformer;

/**
 *
 */
public class MarshallerImplTestCase extends TestCase {
    private MarshallerImpl marshaller;


    public void testPrimitiveDeserialize() throws Exception {
        Method testString = getClass().getDeclaredMethod("testString", String.class);
        Method testShort = getClass().getDeclaredMethod("testShort", Short.class);
        Method testInteger = getClass().getDeclaredMethod("testInteger", Integer.class);
        Method testLong = getClass().getDeclaredMethod("testLong", Long.class);
        Method testDouble = getClass().getDeclaredMethod("testDouble", Double.class);
        Method testFloat = getClass().getDeclaredMethod("testFloat", Float.class);

        assertEquals("test", marshaller.deserialize("test", testString));
        assertEquals((short) 1, marshaller.deserialize("1", testShort));
        assertEquals(1, marshaller.deserialize("1", testInteger));
        assertEquals(Long.MAX_VALUE, marshaller.deserialize(Long.toString(Long.MAX_VALUE), testLong));
        assertEquals(Double.MAX_VALUE, marshaller.deserialize(Double.toString(Double.MAX_VALUE), testDouble));
        assertEquals(1.1f, marshaller.deserialize("1.1", testFloat));

    }

    public void testDeserializeUrlParams() throws Exception {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/management/services/service/123"));
        EasyMock.replay(request);

        Method method = getClass().getDeclaredMethod("testString", String.class);
        ResourceMapping mapping = new ResourceMapping("foo", "management/services/service", "/", Verb.GET, method, null, null, null);
        Object[] params = marshaller.deserialize(Verb.GET, request, mapping);
        assertEquals("123", params[0]);

        EasyMock.verify(request);
    }

    public void testDeserializeHttpRequestUrlParams() throws Exception {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.replay(request);

        Method method = getClass().getDeclaredMethod("testRequest", HttpServletRequest.class);
        ResourceMapping mapping = new ResourceMapping("foo", "management/services/service", "/", Verb.GET, method, null, null, null);
        Object[] params = marshaller.deserialize(Verb.GET, request, mapping);
        assertEquals(request, params[0]);

        EasyMock.verify(request);
    }

    @SuppressWarnings({"unchecked"})
    public void testDeserializeInputStream() throws Exception {
        ServletInputStream stream = new MockInputStream(new ByteArrayInputStream("123".getBytes()));
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getInputStream()).andReturn(stream);
        EasyMock.expect(request.getContentType()).andReturn(Constants.APPLICATION_JSON);

        Method method = getClass().getDeclaredMethod("testString", String.class);
        Transformer transformer = EasyMock.createMock(Transformer.class);
        EasyMock.expect(transformer.transform(EasyMock.isA(MockInputStream.class), EasyMock.isA(ClassLoader.class))).andReturn("123");
        TransformerPair pair = new TransformerPair(transformer, null);

        ResourceMapping mapping = new ResourceMapping("foo", "management/services/service", "/", Verb.GET, method, this, pair, null);
        EasyMock.replay(request, transformer);

        Object[] params = marshaller.deserialize(Verb.POST, request, mapping);
        assertEquals("123", params[0]);

        EasyMock.verify(request, transformer);
    }

    @SuppressWarnings({"unchecked"})
    public void testDeserializeHttpRequestInputStream() throws Exception {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        Method method = getClass().getDeclaredMethod("testRequest", HttpServletRequest.class);

        ResourceMapping mapping = new ResourceMapping("foo", "management/services/service", "/", Verb.GET, method, this, null, null);
        EasyMock.replay(request);

        Object[] params = marshaller.deserialize(Verb.POST, request, mapping);
        assertEquals(request, params[0]);

        EasyMock.verify(request);
    }

    @SuppressWarnings({"unchecked"})
    public void testSerialize() throws Exception {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/management/services/service/123"));
        Method method = getClass().getDeclaredMethod("testRequest", HttpServletRequest.class);

        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
        ServletOutputStream outputStream = new MockOutputStream();

        Transformer transformer = EasyMock.createMock(Transformer.class);
        EasyMock.expect(transformer.transform(EasyMock.isA(MockInputStream.class), EasyMock.isA(ClassLoader.class))).andReturn("123");
        TransformerPair pair = new TransformerPair(null, transformer);

        EasyMock.expect(response.getOutputStream()).andReturn(outputStream);

        ResourceMapping mapping = new ResourceMapping("foo", "management/services/service", "/", Verb.GET, method, this, pair, null);
        EasyMock.replay(request, response);

        marshaller.serialize("123", mapping, request, response);

        EasyMock.verify(request, response);
    }

    @SuppressWarnings({"unchecked"})
    public void testSerializeResource() throws Exception {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/management/services/service/123"));
        Method method = getClass().getDeclaredMethod("testRequest", HttpServletRequest.class);

        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
        ServletOutputStream outputStream = new MockOutputStream();

        Transformer transformer = EasyMock.createMock(Transformer.class);
        EasyMock.expect(transformer.transform(EasyMock.isA(MockInputStream.class), EasyMock.isA(ClassLoader.class))).andReturn("123");
        TransformerPair pair = new TransformerPair(null, transformer);

        EasyMock.expect(response.getOutputStream()).andReturn(outputStream);

        ResourceMapping mapping = new ResourceMapping("foo", "management/services/service", "/", Verb.GET, method, this, pair, null);
        EasyMock.replay(request, response);

        Resource resource = new Resource();
        marshaller.serialize(resource, mapping, request, response);

        EasyMock.verify(request, response);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ManagementMonitor monitor = EasyMock.createNiceMock(ManagementMonitor.class);
        EasyMock.replay(monitor);

        marshaller = new MarshallerImpl(monitor);
    }

    private void testRequest(HttpServletRequest request) {

    }

    private void testString(String param) {

    }

    private void testShort(Short param) {

    }

    private void testInteger(Integer param) {

    }

    private void testLong(Long param) {

    }

    private void testDouble(Double param) {

    }

    private void testFloat(Float param) {

    }

    private class MockInputStream extends ServletInputStream {
        private InputStream stream;

        private MockInputStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return stream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return stream.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return stream.skip(n);
        }

        @Override
        public int available() throws IOException {
            return stream.available();
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }

        @Override
        public void mark(int readlimit) {
            stream.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            stream.reset();
        }

        @Override
        public boolean markSupported() {
            return stream.markSupported();
        }
    }

    private class MockOutputStream extends ServletOutputStream {

        @Override
        public void write(int b) throws IOException {

        }

        @Override
        public void write(byte[] b) throws IOException {
            // ignore
        }
    }

}
