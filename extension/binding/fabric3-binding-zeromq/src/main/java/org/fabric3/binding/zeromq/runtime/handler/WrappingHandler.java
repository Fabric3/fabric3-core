package org.fabric3.binding.zeromq.runtime.handler;

import org.fabric3.spi.channel.EventStreamHandler;

/**
 * Wraps a single event in an Object array. Used when deserializing a parameter from a ZeroMQ socket and sending it down an event stream.
 */
public class WrappingHandler implements EventStreamHandler {
    private EventStreamHandler next;

    public void handle(Object event) {
        next.handle(new Object[]{event});
    }

    public void setNext(EventStreamHandler next) {
        this.next = next;
    }

    public EventStreamHandler getNext() {
        return next;
    }
}
