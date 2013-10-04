package org.fabric3.fabric.binding;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.binding.handler.BindingHandler;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.ScopedComponent;
import org.fabric3.spi.invocation.Message;

/**
 *
 */
public class BindingHandlerLazyLoadDecoratorTestCase extends TestCase {
    private static final URI HANDLER_URI = URI.create("handler");

    private ScopedComponent component;
    private ComponentManager componentManager;
    private BindingHandlerLazyLoadDecorator<Object> decorator;
    private BindingHandler handler;


    public void testLazyLoad() throws Exception {
        EasyMock.expect(componentManager.getComponent(HANDLER_URI)).andReturn(component);
        EasyMock.expect(component.getInstance()).andReturn(handler).times(2);

        EasyMock.replay(component, componentManager, handler);
        decorator.handleOutbound(EasyMock.createNiceMock(Message.class), new Object());
        decorator.handleOutbound(EasyMock.createNiceMock(Message.class), new Object());
        EasyMock.verify(component, componentManager, handler);
    }

    public void setUp() throws Exception {
        super.setUp();
        component = EasyMock.createMock(ScopedComponent.class);
        componentManager = EasyMock.createMock(ComponentManager.class);
        decorator = new BindingHandlerLazyLoadDecorator<Object>(HANDLER_URI, componentManager);
        handler = EasyMock.createNiceMock(BindingHandler.class);

    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
}
