package org.fabric3.binding.jms.runtime.common;

/**
 *
 */
public interface JmsRuntimeConstants {

    /**
     * Header used to specify the service operation name being invoked.
     */
    String OPERATION_HEADER = "scaOperationName";

    /**
     * Header used to determine if a response is a fault.
     */
    String FAULT_HEADER = "f3Fault";

    /**
     * Header for storing callback context
     */
    String CONTEXT_HEADER = "f3Context";

    /**
     * No caching of JMS objects
     */
    int CACHE_NONE = 0;

    /**
     * JMS connection caching
     */
    int CACHE_CONNECTION = 1;

    /**
     * Caching of all JMS objects
     */
    int CACHE_ADMINISTERED_OBJECTS = 2;
}
