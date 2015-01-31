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
package org.fabric3.implementation.pojo.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.fabric3.api.host.ContainerException;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvoker;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.InvocationRuntimeException;

public class InvokerInterceptorBasicTestCase extends TestCase {
    private ServiceInvoker echoTargetInvoker;
    private ServiceInvoker arrayTargetInvoker;
    private ServiceInvoker nullParamTargetInvoker;
    private ServiceInvoker primitiveTargetInvoker;
    private ServiceInvoker checkedTargetInvoker;
    private ServiceInvoker runtimeTargetInvoker;

    private IMocksControl control;
    private Object instance;
    private AtomicComponent component;
    private Message message;

    public void testObjectInvoke() throws Throwable {
        String value = "foo";
        mockCall(new Object[]{value}, value);
        control.replay();
        InvokerInterceptor invoker = new InvokerInterceptor(echoTargetInvoker, component);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testPrimitiveInvoke() throws Throwable {
        Integer value = 1;
        mockCall(new Object[]{value}, value);
        control.replay();
        InvokerInterceptor invoker = new InvokerInterceptor(primitiveTargetInvoker, component);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testArrayInvoke() throws Throwable {
        String[] value = new String[]{"foo", "bar"};
        mockCall(new Object[]{value}, value);
        control.replay();
        InvokerInterceptor invoker = new InvokerInterceptor(arrayTargetInvoker, component);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testEmptyInvoke() throws Throwable {
        mockCall(new Object[]{}, "foo");
        control.replay();
        InvokerInterceptor invoker = new InvokerInterceptor(nullParamTargetInvoker, component);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testNullInvoke() throws Throwable {
        mockCall(null, "foo");
        control.replay();
        InvokerInterceptor invoker = new InvokerInterceptor(nullParamTargetInvoker, component);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testInvokeCheckedException() throws Throwable {
        mockFaultCall(null, TestException.class);
        control.replay();
        InvokerInterceptor invoker = new InvokerInterceptor(checkedTargetInvoker, component);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testInvokeRuntimeException() throws Throwable {
        mockFaultCall(null, TestRuntimeException.class);
        control.replay();
        InvokerInterceptor invoker = new InvokerInterceptor(runtimeTargetInvoker, component);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testFailureGettingWrapperThrowsException() {
        ContainerException ex = new ContainerException("test");
        try {
            EasyMock.expect(component.getInstance()).andThrow(ex);
        } catch (ContainerException e) {
            throw new AssertionError();
        }
        control.replay();
        try {
            InvokerInterceptor invoker = new InvokerInterceptor(echoTargetInvoker, component);
            invoker.invoke(message);
            fail();
        } catch (InvocationRuntimeException e) {
            assertSame(ex, e.getCause());
            control.verify();
        }
    }

    private void mockCall(Object value, Object body) throws Exception {
        EasyMock.expect(component.getInstance()).andReturn(instance);
        EasyMock.expect(message.getBody()).andReturn(value);
        message.setBody(body);
        component.releaseInstance(instance);
    }

    private void mockFaultCall(Object value, Class<? extends Exception> fault) throws Exception {
        EasyMock.expect(component.getInstance()).andReturn(instance);
        EasyMock.expect(message.getBody()).andReturn(value);
        message.setBodyWithFault(EasyMock.isA(fault));
        component.releaseInstance(instance);
    }

    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        instance = new TestBean();
        echoTargetInvoker = new MockInvoker(TestBean.class.getDeclaredMethod("echo", String.class));
        arrayTargetInvoker = new MockInvoker(TestBean.class.getDeclaredMethod("arrayEcho", String[].class));
        nullParamTargetInvoker = new MockInvoker(TestBean.class.getDeclaredMethod("nullParam"));
        primitiveTargetInvoker = new MockInvoker(TestBean.class.getDeclaredMethod("primitiveEcho", Integer.TYPE));
        checkedTargetInvoker = new MockInvoker(TestBean.class.getDeclaredMethod("checkedException"));
        runtimeTargetInvoker = new MockInvoker(TestBean.class.getDeclaredMethod("runtimeException"));
        assertNotNull(echoTargetInvoker);
        assertNotNull(checkedTargetInvoker);
        assertNotNull(runtimeTargetInvoker);

        control = EasyMock.createStrictControl();
        component = control.createMock(AtomicComponent.class);
        message = control.createMock(Message.class);
    }

    private class TestBean {

        public String echo(String msg) throws Exception {
            assertEquals("foo", msg);
            return msg;
        }

        public String[] arrayEcho(String[] msg) throws Exception {
            assertNotNull(msg);
            assertEquals(2, msg.length);
            assertEquals("foo", msg[0]);
            assertEquals("bar", msg[1]);
            return msg;
        }

        public String nullParam() throws Exception {
            return "foo";
        }

        public int primitiveEcho(int i) throws Exception {
            return i;
        }

        public void checkedException() throws TestException {
            throw new TestException();
        }

        public void runtimeException() throws TestRuntimeException {
            throw new TestRuntimeException();
        }
    }

    public static class TestException extends Exception {
        private static final long serialVersionUID = 7608600189165212994L;
    }

    public static class TestRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 4645804600571852557L;
    }

    private class MockInvoker implements ServiceInvoker {
        private Method method;

        private MockInvoker(Method method) {
            this.method = method;
        }

        public Object invoke(Object obj, Object args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return method.invoke(obj, (Object[]) args);
        }
    }
}
