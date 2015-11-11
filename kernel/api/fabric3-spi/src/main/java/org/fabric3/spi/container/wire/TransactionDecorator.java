package org.fabric3.spi.container.wire;

/**
 * Adds an transaction interceptor to the chain.
 *
 * This class is used to decouple binding and implementation extensions from the underlying transaction API and may not be available in all runtime
 * configurations.
 */
public interface TransactionDecorator {

    /**
     * Adds a transaction interceptor to the chain.
     *
     * @param chain the chain
     */
    void transactional(InvocationChain chain);

}
