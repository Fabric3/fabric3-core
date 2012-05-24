package org.fabric3.binding.ws.metro.runtime.core;

import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.binding.handler.BindingHandler;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;


/**
 * {@link BindingHandler} Adapter for JAX-WS {@link SOAPHandler}.
 *
 * @version $Rev$ $Date$
 */
public class SOAPMessageHandlerAdapter implements SOAPHandler<SOAPMessageContext> {

    private BindingHandler<SOAPMessage> delegateHandler;

    public SOAPMessageHandlerAdapter(BindingHandler<?> handler) {
        delegateHandler = (BindingHandler) handler;
    }

    public void close(MessageContext mc) {
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        workContext = (WorkContext) (workContext == null ? smc.get(MetroConstants.WORK_CONTEXT) : workContext);
        if (workContext == null) {
            throw new AssertionError("Work context not set");
        }
        MessageImpl msg = new MessageImpl(smc.getMessage(), false, workContext);
        if (outbound) {
            delegateHandler.handleOutbound(msg, smc.getMessage());
        } else {
            delegateHandler.handleInbound(smc.getMessage(), msg);
        }
        return true;
    }

    public Set<QName> getHeaders() {
        return null;
    }

}
