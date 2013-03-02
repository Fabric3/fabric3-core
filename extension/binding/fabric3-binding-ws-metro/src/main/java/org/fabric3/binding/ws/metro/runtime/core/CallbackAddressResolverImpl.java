package org.fabric3.binding.ws.metro.runtime.core;

import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 *
 */
public class CallbackAddressResolverImpl implements CallbackAddressResolver {
    public URL resolveUrl() {
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        try {
            return new URL(workContext.getHeader(String.class, CallbackConstants.ENDPOINT_ADDRESS));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
