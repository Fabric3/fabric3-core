package org.fabric3.binding.zeromq.runtime.interceptor;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;

/**
 * Unwraps a single message parameter from an Object array to a raw (only the parameter) form. Used when serializing a message to the ZeroMQ socket.
 */
public class UnwrappingInterceptor implements Interceptor {
    private Interceptor next;

    public Message invoke(Message msg) {
        Object body = msg.getBody();
        if (body == null || !body.getClass().isArray()) {
            return next.invoke(msg);
        }
        Object[] payload = (Object[]) body;
        if (payload.length != 1) {
            throw new ServiceRuntimeException("Unexpected payload size: " + payload.length);
        }
        msg.setBody(payload[0]);
        return next.invoke(msg);
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }
}
