package org.fabric3.execution.runtime;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * Propagates a work context to the current thread executing the delegate runnable.
 */
public class PropagatingRunnable implements Runnable {
    private Runnable delegate;
    private WorkContext context;

    public PropagatingRunnable(Runnable delegate, WorkContext context) {
        this.delegate = delegate;
        this.context = context;
    }

    public void run() {
        WorkContext old = WorkContextTunnel.setThreadWorkContext(context);
        try {
            delegate.run();
        } finally {
            WorkContextTunnel.setThreadWorkContext(old);
        }
    }
}
