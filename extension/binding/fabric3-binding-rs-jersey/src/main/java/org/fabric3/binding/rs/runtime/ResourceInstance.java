package org.fabric3.binding.rs.runtime;

import java.util.Map;

import org.fabric3.spi.wire.InvocationChain;

/**
 * Used to represent an F3 component instance in places where a resource instance must be passed to Jersey.
 */
public class ResourceInstance {
    private Map<String, InvocationChain> chains;
    private boolean authenticate;

    public ResourceInstance(Map<String, InvocationChain> chains, boolean authenticate) {
        this.chains = chains;
        this.authenticate = authenticate;
    }

    public Map<String, InvocationChain> getChains() {
        return chains;
    }

    public boolean authenticate() {
        return authenticate;
    }
}
