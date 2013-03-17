package org.fabric3.implementation.bytecode.proxy.common;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public class ProxyFactoryImplTestCase extends TestCase {
    public static final URI URI = java.net.URI.create("test");
    private ProxyFactoryImpl factory;

    public void testProxyDispatch() throws Exception {
        Method[] methods = ProxyInterface.class.getMethods();

        boolean[] invoked = new boolean[]{false, false, false};
        ProxyInterface proxy = factory.createProxy(URI, ProxyInterface.class, methods, MockDispatcher.class, true);
        ((MockDispatcher) proxy).init(invoked);

        proxy.handle("event");
        proxy.handle(1d);
        proxy.handle(new Object());

        MockDispatcher handler = (MockDispatcher) proxy;
        assertTrue(handler.invoked[0]);
        assertTrue(handler.invoked[1]);
        assertTrue(handler.invoked[2]);
    }

    public void testReturnDispatch() throws Exception {
        Method[] methods = ProxyReturnInterface.class.getMethods();

        ProxyReturnInterface proxy = factory.createProxy(URI, ProxyReturnInterface.class, methods, MockReturningDispatcher.class, true);
        assertEquals("test", proxy.handle("test"));
    }

    public void testCheckedExceptionDispatch() throws Exception {
        Method[] methods = ProxyCheckedExceptionInterface.class.getMethods();

        ProxyCheckedExceptionInterface proxy = factory.createProxy(URI, ProxyCheckedExceptionInterface.class, methods, MockCheckedExceptionDispatcher.class,
                                                                   true);
        try {
            proxy.handle("test");
            fail();
        } catch (IOException e) {
            // expected
        }
    }

    public void testCheckedRuntimeExceptionDispatch() throws Exception {
        Method[] methods = ProxyCheckedExceptionInterface.class.getMethods();

        ProxyRuntimeExceptionInterface proxy = factory.createProxy(URI, ProxyRuntimeExceptionInterface.class, methods, MockRuntimeExceptionDispatcher.class,
                                                                   true);
        try {
            proxy.handle("test");
            fail();
        } catch (ServiceRuntimeException e) {
            // expected
        }
    }

    public void testNoParamDispatch() throws Exception {
        Method[] methods = ProxyNoParamInterface.class.getMethods();

        ProxyNoParamInterface proxy = factory.createProxy(URI, ProxyNoParamInterface.class, methods, MockReturningDispatcher.class, true);
        assertEquals("test", proxy.get());
    }

    public void testPrimitivesReturnDispatch() throws Exception {
        Method[] methods = ProxyPrimitivesInterface.class.getMethods();

        ProxyPrimitivesInterface proxy = factory.createProxy(URI, ProxyPrimitivesInterface.class, methods, MockEchoDispatcher.class, true);

        assertEquals(Double.MAX_VALUE, proxy.getDoublePrimitive(Double.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, proxy.getIntPrimitive(Integer.MAX_VALUE));
        assertEquals(Short.MAX_VALUE, proxy.getShortPrimitive(Short.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, proxy.getLongPrimitive(Long.MAX_VALUE));
        assertEquals(Float.MAX_VALUE, proxy.getFloatPrimitive(Float.MAX_VALUE));
        assertEquals(Byte.MAX_VALUE, proxy.getBytePrimitive(Byte.MAX_VALUE));
        assertTrue(proxy.getBooleanPrimitive(true));
    }

    public void setUp() throws Exception {
        super.setUp();

        ClassLoaderRegistry registry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(registry.getClassLoader(EasyMock.isA(URI.class))).andReturn(getClass().getClassLoader());
        EasyMock.replay(registry);

        factory = new ProxyFactoryImpl(registry);
    }

}

