package org.fabric3.fabric.binding;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.namespace.QName;

import org.fabric3.spi.binding.handler.BindingHandler;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopedComponent;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContextTunnel;

public class BindingHandlerLazyLoadDecorator<T> implements BindingHandler<T> {
	
	private final URI delegateURI;
	private final ComponentManager componentManager;
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private BindingHandler<T> delegate;

	public BindingHandlerLazyLoadDecorator(URI delegateURI, ComponentManager componentManager) {
		this.delegateURI = delegateURI;
		this.componentManager = componentManager;
	}

	public QName getType() {
		return inject().getType();
	}

	public void handleOutbound(Message message, T context) {
		inject().handleOutbound(message, context);

	}

	public void handleInbound(T context, Message message) {
		inject().handleInbound(context, message);
	}
	
	private BindingHandler<T> inject() {
		if (!initialized.getAndSet(true) || delegate == null){
			ScopedComponent handlerDelegateComponent = (ScopedComponent) componentManager.getComponent(URI.create("fabric3://domain/"+delegateURI.toString()));
			try {
				this.delegate =  (BindingHandler<T>) handlerDelegateComponent.getInstance(WorkContextTunnel.getThreadWorkContext());
			} catch (InstanceLifecycleException e) {
				throw new RuntimeException(e);
			}
		}
		return this.delegate;
	}

}
