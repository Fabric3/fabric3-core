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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.transport.jetty.impl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.runtime.event.EventService;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

/**
 *
 */
public class JettyServiceImplTestCase extends TestCase {

    private static final String REQUEST1_HEADER = "GET / HTTP/1.0\n" + "Host: localhost\n" + "Content-Type: text/xml\n" + "Connection: close\n"
                                                  + "Content-Length: ";
    private static final String REQUEST1_CONTENT = "";
    private static final String REQUEST1 = REQUEST1_HEADER + REQUEST1_CONTENT.getBytes().length + "\n\n" + REQUEST1_CONTENT;

    private static final int HTTP_PORT = 8585;

    private ExecutorService executor = Executors.newCachedThreadPool();
    private JettyServiceImpl service;

    /**
     * Verifies requests are properly routed according to the servlet mapping
     */
    public void testRegisterServletMapping() throws Exception {
        service.setHttpPort(String.valueOf(HTTP_PORT));
        service.init();
        TestServlet servlet = new TestServlet();
        service.registerMapping("/", servlet);
        assertTrue(service.isMappingRegistered("/"));
        Socket client = new Socket("127.0.0.1", HTTP_PORT);
        OutputStream os = client.getOutputStream();
        os.write(REQUEST1.getBytes());
        os.flush();
        read(client);
        service.destroy();
        assertTrue(servlet.invoked);
    }

    public void testUnRegisterServletMapping() throws Exception {
        service.setHttpPort(String.valueOf(HTTP_PORT));
        service.init();
        assertFalse(service.isMappingRegistered("/"));
        TestServlet servlet = new TestServlet();
        service.registerMapping("/", servlet);
        assertEquals(servlet, service.unregisterMapping("/"));
        assertFalse(service.isMappingRegistered("/"));
        service.destroy();
    }

    //    public void testRequestSession() throws Exception {
    //        JettyServiceImpl service = new JettyServiceImpl(monitor, scheduler);
    //        service.setDebug(true);
    //        service.setHttpPort(HTTP_PORT);
    //        service.init();
    //        TestServlet servlet = new TestServlet();
    //        service.registerMapping("/", servlet);
    //        Socket client = new Socket("127.0.0.1", HTTP_PORT);
    //        OutputStream os = client.getOutputStream();
    //        os.write(REQUEST1.getBytes());
    //        os.flush();
    //        read(client);
    //        service.destroy();
    //        assertTrue(servlet.invoked);
    //        assertNotNull(servlet.sessionId);
    //    }
    //
    //    public void testUseWorkScheduler() throws Exception {
    //        JettyServiceImpl service = new JettyServiceImpl(monitor, scheduler);
    //        service.setDebug(true);
    //        service.setHttpPort(HTTP_PORT);
    //        service.init();
    //        TestServlet servlet = new TestServlet();
    //        service.registerMapping("/", servlet);
    //        Socket client = new Socket("127.0.0.1", HTTP_PORT);
    //        OutputStream os = client.getOutputStream();
    //        os.write(REQUEST1.getBytes());
    //        os.flush();
    //        read(client);
    //        service.destroy();
    //        assertTrue(servlet.invoked);
    //    }

    public void testRestart() throws Exception {
        service.setHttpPort(String.valueOf(HTTP_PORT));
        service.init();
        service.destroy();
        service.init();
        service.destroy();
    }

    public void testNoMappings() throws Exception {
        service.setHttpPort(String.valueOf(HTTP_PORT));
        service.init();
        Socket client = new Socket("127.0.0.1", HTTP_PORT);
        OutputStream os = client.getOutputStream();
        os.write(REQUEST1.getBytes());
        os.flush();
        read(client);
        service.destroy();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        //executor.submit();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        TransportMonitor monitor = createMock(TransportMonitor.class);
        ExecutorService executorService = createMock(ExecutorService.class);
        executorService.execute(isA(Runnable.class));

        expectLastCall().andStubAnswer(new IAnswer() {
            public Object answer() throws Throwable {
                Runnable runnable = (Runnable) getCurrentArguments()[0];
                executor.execute(runnable);
                return null;
            }
        });
        replay(executorService);
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime").atLeastOnce();
        EasyMock.replay(info);

        Port port = EasyMock.createMock(Port.class);
        EasyMock.expect(port.getNumber()).andReturn(8585).anyTimes();
        port.bind(Port.TYPE.TCP);
        EasyMock.expectLastCall().atLeastOnce();

        PortAllocator portAllocator = EasyMock.createMock(PortAllocator.class);
        EasyMock.expect(portAllocator.isPoolEnabled()).andReturn(false).times(2);
        EasyMock.expect(portAllocator.reserve("HTTP", "HTTP", 8585)).andReturn(port).atLeastOnce();
        portAllocator.release("HTTP");
        EasyMock.expectLastCall().atLeastOnce();
        EasyMock.replay(portAllocator, port);

        EventService eventService = EasyMock.createNiceMock(EventService.class);

        service = new JettyServiceImpl(portAllocator, monitor, eventService, info);

    }

    private static String read(Socket socket) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private class TestServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        boolean invoked;
        String sessionId;

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            invoked = true;
            sessionId = req.getSession().getId();
            try (OutputStream writer = resp.getOutputStream()) {
                writer.write("result".getBytes());
            }
        }

    }
}
