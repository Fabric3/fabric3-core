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
package org.fabric3.implementation.pojo.component;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;

import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationRuntimeException;

public class InvokerInterceptorBasicTestCase extends TestCase {
    private TestBean bean;
    private Method echoMethod;
    private Method arrayMethod;
    private Method nullParamMethod;
    private Method primitiveMethod;
    private Method checkedMethod;
    private Method runtimeMethod;

    private IMocksControl control;
    private WorkContext workContext;
    private ScopeContainer scopeContainer;
    private InstanceWrapper wrapper;
    private AtomicComponent component;
    private Message message;

    public void testObjectInvoke() throws Throwable {
        String value = "foo";
        mockCall(new Object[]{value}, value);
        control.replay();
        InvokerInterceptor invoker =
                new InvokerInterceptor(echoMethod, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testPrimitiveInvoke() throws Throwable {
        Integer value = 1;
        mockCall(new Object[]{value}, value);
        control.replay();
        InvokerInterceptor invoker =
                new InvokerInterceptor(primitiveMethod, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testArrayInvoke() throws Throwable {
        String[] value = new String[]{"foo", "bar"};
        mockCall(new Object[]{value}, value);
        control.replay();
        InvokerInterceptor invoker =
                new InvokerInterceptor(arrayMethod, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testEmptyInvoke() throws Throwable {
        mockCall(new Object[]{}, "foo");
        control.replay();
        InvokerInterceptor invoker =
                new InvokerInterceptor(nullParamMethod, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testNullInvoke() throws Throwable {
        mockCall(null, "foo");
        control.replay();
        InvokerInterceptor invoker =
                new InvokerInterceptor(nullParamMethod, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testInvokeCheckedException() throws Throwable {
        mockFaultCall(null, TestException.class);
        control.replay();
        InvokerInterceptor invoker =
                new InvokerInterceptor(checkedMethod, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testInvokeRuntimeException() throws Throwable {
        mockFaultCall(null, TestRuntimeException.class);
        control.replay();
        InvokerInterceptor invoker =
                new InvokerInterceptor(runtimeMethod, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testFailureGettingWrapperThrowsException() {
        EasyMock.expect(message.getWorkContext()).andReturn(workContext);
        InstanceLifecycleException ex = new InstanceLifecycleException(null);
        try {
            EasyMock.expect(scopeContainer.getWrapper(component, workContext)).andThrow(ex);
        } catch (ComponentException e) {
            throw new AssertionError();
        }
        control.replay();
        try {
            InvokerInterceptor invoker =
                    new InvokerInterceptor(echoMethod, component, scopeContainer);
            invoker.invoke(message);
            fail();
        } catch (InvocationRuntimeException e) {
            assertSame(ex, e.getCause());
            control.verify();
        }
    }

    private void mockCall(Object value, Object body) throws Exception {
        EasyMock.expect(message.getWorkContext()).andReturn(workContext);
        EasyMock.expect(scopeContainer.getWrapper(component, workContext)).andReturn(wrapper);
        EasyMock.expect(wrapper.getInstance()).andReturn(bean);
        EasyMock.expect(message.getBody()).andReturn(value);
        message.setBody(body);
        scopeContainer.returnWrapper(component, workContext, wrapper);
    }

    private void mockFaultCall(Object value, Class<? extends Exception> fault) throws Exception {
        EasyMock.expect(message.getWorkContext()).andReturn(workContext);
        EasyMock.expect(scopeContainer.getWrapper(component, workContext)).andReturn(wrapper);
        EasyMock.expect(wrapper.getInstance()).andReturn(bean);
        EasyMock.expect(message.getBody()).andReturn(value);
        message.setBodyWithFault(EasyMock.isA(fault));
        scopeContainer.returnWrapper(component, workContext, wrapper);
    }

    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        bean = new TestBean();
        echoMethod = TestBean.class.getDeclaredMethod("echo", String.class);
        arrayMethod = TestBean.class.getDeclaredMethod("arrayEcho", String[].class);
        nullParamMethod = TestBean.class.getDeclaredMethod("nullParam");
        primitiveMethod = TestBean.class.getDeclaredMethod("primitiveEcho", Integer.TYPE);
        checkedMethod = TestBean.class.getDeclaredMethod("checkedException");
        runtimeMethod = TestBean.class.getDeclaredMethod("runtimeException");
        assertNotNull(echoMethod);
        assertNotNull(checkedMethod);
        assertNotNull(runtimeMethod);

        control = EasyMock.createStrictControl();
        workContext = control.createMock(WorkContext.class);
        component = control.createMock(AtomicComponent.class);
        scopeContainer = control.createMock(ScopeContainer.class);
        wrapper = control.createMock(InstanceWrapper.class);
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
}
