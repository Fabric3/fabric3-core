package org.fabric3.binding.ws.metro.runtime.core;

import org.fabric3.api.annotation.monitor.Debug;

/**
 */
public interface InterceptorMonitor {

    @Debug("Service unavailable. Attempting retry")
    void serviceUnavailableRetry(Throwable t);
}
