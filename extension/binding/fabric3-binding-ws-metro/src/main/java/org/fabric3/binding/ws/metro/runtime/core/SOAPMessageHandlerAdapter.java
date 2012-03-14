package org.fabric3.binding.ws.metro.runtime.core;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.fabric3.spi.binding.handler.BindingHandler;
import org.fabric3.spi.invocation.MessageImpl;


public class SOAPMessageHandlerAdapter implements SOAPHandler<SOAPMessageContext>{

	private BindingHandler<SOAPMessage> delegateHandler;
	
	public SOAPMessageHandlerAdapter(BindingHandler<?> h) {
		delegateHandler = (BindingHandler<SOAPMessage>) h;
	}

	public void close(MessageContext mc) {
	}

	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		MessageImpl msg = new MessageImpl();
		msg.setBody(smc.getMessage());
		if (outbound){
			delegateHandler.handleOutbound(msg,smc.getMessage());
		}
		else {
			delegateHandler.handleInbound(smc.getMessage(),msg);
		}
		return true;
	}

	public Set<QName> getHeaders() {
		return null;
	}

}
