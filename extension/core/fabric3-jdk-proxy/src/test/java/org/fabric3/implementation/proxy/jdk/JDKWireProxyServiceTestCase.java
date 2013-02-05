/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.implementation.proxy.jdk;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.implementation.proxy.jdk.wire.JDKInvocationHandler;
import org.fabric3.implementation.proxy.jdk.wire.JDKWireProxyService;
import org.oasisopen.sca.ServiceReference;

import org.fabric3.spi.wire.InvocationChain;

/**
 *
 */
public class JDKWireProxyServiceTestCase extends TestCase {
    private JDKWireProxyService proxyService;

    public void testCastProxyToServiceReference() throws Exception {
        Map<Method, InvocationChain> mapping = Collections.emptyMap();
        JDKInvocationHandler<Foo> handler = new JDKInvocationHandler<Foo>(Foo.class, null, mapping);
        Foo proxy = handler.getService();
        ServiceReference<Foo> ref = proxyService.cast(proxy);
        assertSame(handler, ref);
    }

    public void testEquals() throws Exception {
        Map<Method, InvocationChain> mapping = new HashMap<Method, InvocationChain>();
        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.replay(chain);

        mapping.put(Foo.class.getMethod("invoke"), chain);

        JDKInvocationHandler<Foo> handler1 = new JDKInvocationHandler<Foo>(Foo.class, null, mapping);
        JDKInvocationHandler<Foo> handler2 = new JDKInvocationHandler<Foo>(Foo.class, null, mapping);

        Object proxy1 = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Foo.class}, handler1);
        Object proxy2 = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Foo.class}, handler2);
        assertTrue(proxy1.equals(proxy2));
        EasyMock.verify(chain);
    }

    protected void setUp() throws Exception {
        super.setUp();
        proxyService = new JDKWireProxyService(null);
    }

    public interface Foo {
        void invoke();

    }


}
