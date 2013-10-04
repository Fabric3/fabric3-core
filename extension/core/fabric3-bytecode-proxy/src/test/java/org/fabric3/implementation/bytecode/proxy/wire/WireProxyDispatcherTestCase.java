package org.fabric3.implementation.bytecode.proxy.wire;

import java.io.IOException;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.implementation.bytecode.proxy.common.ProxyFactoryImpl;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageImpl;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;

/**
 *
 */
public class WireProxyDispatcherTestCase extends TestCase {
    public static final java.net.URI URI = java.net.URI.create("test");

    private ProxyFactoryImpl factory;
    private InvocationChain chain;
    private InvocationChain[] chains;
    private Interceptor interceptor;

    @SuppressWarnings("unchecked")
    public void testProxyDispatch() throws Exception {
        Method[] methods = ProxyInterface.class.getMethods();

        MessageImpl message = new MessageImpl();
        message.setBody("test");
        EasyMock.expect(interceptor.invoke(EasyMock.isA(Message.class))).andReturn(message);
        EasyMock.replay(chain, interceptor);

        ProxyInterface proxy = factory.createProxy(URI, ProxyInterface.class, methods, WireProxyDispatcher.class, true);
        ((WireProxyDispatcher) proxy).init(ProxyInterface.class, null, chains);
        assertEquals("test", proxy.invoke("test"));

    }

    @SuppressWarnings("unchecked")
    public void testFaultDispatch() throws Exception {
        Method[] methods = ProxyInterface.class.getMethods();

        MessageImpl message = new MessageImpl();
        message.setBodyWithFault(new IOException());
        EasyMock.expect(interceptor.invoke(EasyMock.isA(Message.class))).andReturn(message);
        EasyMock.replay(chain, interceptor);

        FaultProxyInterface proxy = factory.createProxy(URI, FaultProxyInterface.class, methods, WireProxyDispatcher.class, true);
        ((WireProxyDispatcher) proxy).init(ProxyInterface.class, null, chains);

        try {
            proxy.invoke("test");
            fail();
        } catch (IOException e) {
            //expected
        }

    }

    protected void setUp() throws Exception {
        super.setUp();

        ClassLoaderRegistry registry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(registry.getClassLoader(EasyMock.isA(java.net.URI.class))).andReturn(getClass().getClassLoader());
        EasyMock.replay(registry);

        factory = new ProxyFactoryImpl(registry);

        interceptor = EasyMock.createMock(Interceptor.class);
        chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getHeadInterceptor()).andReturn(interceptor).anyTimes();

        chains = new InvocationChain[]{chain};
    }

    public interface ProxyInterface {

        String invoke(String message);

    }

    public interface FaultProxyInterface {

        String invoke(String message) throws IOException;
    }

}
