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

/**
 * {@link BindingHandler} decorator performs final connection between Binding and target {@link BindingHandler}.
 * Lazy loading is used to ensure the full initialization of the both. 
 * 
 * @author palmalcheg
 *
 * @param <T>
 */
public class BindingHandlerLazyLoadDecorator<T> implements BindingHandler<T> {
	
	private static final String FABRIC3_DOMAIN = "fabric3://domain/";
	private final URI targetBindingHandlerURI;
	private final ComponentManager componentManager;
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private BindingHandler<T> delegate;

	public BindingHandlerLazyLoadDecorator(URI uri, ComponentManager componentManager) {
		this.targetBindingHandlerURI = uri;
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
	
	@SuppressWarnings("unchecked")
	private BindingHandler<T> inject() {
		if (!initialized.getAndSet(true) || delegate == null){
			ScopedComponent handlerDelegateComponent = (ScopedComponent) componentManager.getComponent(URI.create(FABRIC3_DOMAIN+targetBindingHandlerURI.toString()));
			try {
				if (handlerDelegateComponent == null) {
					throw new IllegalStateException("Domain component with a name "+targetBindingHandlerURI.toString() + " doesn't exists.");
				}
				this.delegate =  (BindingHandler<T>) handlerDelegateComponent.getInstance(WorkContextTunnel.getThreadWorkContext());
			} catch (InstanceLifecycleException e) {
				throw new RuntimeException(e);
			}
		}
		return this.delegate;
	}

}
