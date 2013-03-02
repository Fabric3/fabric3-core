package org.fabric3.binding.ws.metro.runtime.core;

import java.net.URL;

/**
 * Dynamically resolves the callback endpoint address from the current request context.
 */
public interface CallbackAddressResolver {

    /**
     * Resolves the callback address.
     *
     * @return the callback address
     */
    URL resolveUrl();

}
