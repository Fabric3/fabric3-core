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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.transport.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.transport.jetty.impl.JettyServiceImpl;
import org.fabric3.transport.jetty.impl.TransportMonitor;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

/**
 * @version $Rev$ $Date$
 */
public class JettyServiceImplTestCase extends TestCase {

    private static final String REQUEST1_HEADER =
            "GET / HTTP/1.0\n"
                    + "Host: localhost\n"
                    + "Content-Type: text/xml\n"
                    + "Connection: close\n"
                    + "Content-Length: ";
    private static final String REQUEST1_CONTENT =
            "";
    private static final String REQUEST1 =
            REQUEST1_HEADER + REQUEST1_CONTENT.getBytes().length + "\n\n" + REQUEST1_CONTENT;

    private static final int HTTP_PORT = 8585;

    private TransportMonitor monitor;
    private ExecutorService executorService;
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
        monitor = createMock(TransportMonitor.class);
        executorService = createMock(ExecutorService.class);
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
        service = new JettyServiceImpl(monitor, info);

    }

    private static String read(Socket socket) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuffer sb = new StringBuffer();
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
            OutputStream writer = resp.getOutputStream();
            try {
                writer.write("result".getBytes());
            } finally {
                writer.close();
            }
        }


    }
}
