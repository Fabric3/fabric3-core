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
package org.fabric3.management.rest.runtime;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.management.rest.Constants;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.management.rest.transformer.TransformerPair;
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
