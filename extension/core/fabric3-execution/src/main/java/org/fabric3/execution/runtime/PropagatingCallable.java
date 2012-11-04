package org.fabric3.execution.runtime;

import java.util.concurrent.Callable;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * Propagates a work context to the current thread executing the delegate callable.
 */
public class PropagatingCallable<T> implements Callable<T> {
    private Callable<T> delegate;
    private WorkContext context;

    public PropagatingCallable(Callable<T> delegate, WorkContext context) {
        this.delegate = delegate;
        this.context = context;
    }

    public T call() throws Exception {
        WorkContext old = WorkContextTunnel.setThreadWorkContext(context);
        try {
            return delegate.call();
        } finally {
            WorkContextTunnel.setThreadWorkContext(old);
        }
    }
}
