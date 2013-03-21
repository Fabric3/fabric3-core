package org.fabric3.implementation.bytecode.proxy.common;

/**
 *
 */
public class MockUnwrappedDispatcher implements ProxyDispatcher {

    public Object _f3_invoke(int index, Object param) throws Exception {
        return param;
    }
}