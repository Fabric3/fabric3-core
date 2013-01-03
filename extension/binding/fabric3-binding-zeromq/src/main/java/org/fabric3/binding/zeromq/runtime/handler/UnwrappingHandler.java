package org.fabric3.binding.zeromq.runtime.handler;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.channel.EventStreamHandler;

/**
 * Unwraps an event from an Object array to a raw (only the event type) form. Used when serializing an event to the ZeroMQ socket.
 */
public class UnwrappingHandler implements EventStreamHandler {
    private EventStreamHandler next;

    public void handle(Object event) {
        if (event == null || !event.getClass().isArray()) {
            throw new ServiceRuntimeException("Expected object array type");
        }
        Object[] eventArray = (Object[]) event;
        if (eventArray.length != 1) {
            throw new ServiceRuntimeException("Unexpected array size: " + eventArray.length);
        }
        next.handle(eventArray[0]);
    }

    public void setNext(EventStreamHandler next) {
        this.next = next;
    }

    public EventStreamHandler getNext() {
        return next;
    }
}
