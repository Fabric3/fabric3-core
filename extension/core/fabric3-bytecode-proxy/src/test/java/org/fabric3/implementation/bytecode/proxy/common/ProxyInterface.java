package org.fabric3.implementation.bytecode.proxy.common;

/**
 *
 */
public interface ProxyInterface {

    void handle(String event);

    void handle(Double event);

    void handle(Object event);

}
