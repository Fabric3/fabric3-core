package org.fabric3.binding.zeromq.runtime.interceptor;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;

/**
 * Wraps a single message parameter in an Object array. Used when deserializing a parameter from a ZeroMQ socket and placing it in a message body.
 */
public class WrappingInterceptor implements Interceptor {
    private Interceptor next;

    public Message invoke(Message msg) {
        msg.setBody(new Object[]{msg.getBody()});
        return next.invoke(msg);
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }
}
