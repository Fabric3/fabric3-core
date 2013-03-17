package org.fabric3.implementation.bytecode.proxy.common;

/**
 *
 */
public class MockDispatcher implements ProxyDispatcher {
    public boolean[] invoked;

    public void init(boolean[] invoked) {
        this.invoked = invoked;

    }

    public Object _f3_invoke(int index, Object params) throws Exception {
        invoked[index] = true;
        return null;
    }
}