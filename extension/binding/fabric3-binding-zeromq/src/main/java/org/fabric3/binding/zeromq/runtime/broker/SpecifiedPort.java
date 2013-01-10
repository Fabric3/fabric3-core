package org.fabric3.binding.zeromq.runtime.broker;

import org.fabric3.spi.host.Port;

/**
 * A explicitly specified port.
 */
public class SpecifiedPort implements Port {
    private int number;

    public SpecifiedPort(int number) {
        this.number = number;
    }

    public String getName() {
        return "zmq.remote";
    }

    public int getNumber() {
        return number;
    }

    public void bind(TYPE type) {
        // no-op
    }

    public void release() {
        // no-op
    }
}
