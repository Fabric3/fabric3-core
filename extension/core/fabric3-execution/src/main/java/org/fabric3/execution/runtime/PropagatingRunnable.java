package org.fabric3.execution.runtime;

import java.util.List;
import java.util.Map;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;

/**
 * Propagates a work context to the current thread executing the delegate runnable.
 */
public class PropagatingRunnable implements Runnable {
    private Runnable delegate;
    private List<CallbackReference> stack;
    private Map<String, Object> headers;
    private SecuritySubject subject;

    public PropagatingRunnable(Runnable delegate, List<CallbackReference> stack, Map<String, Object> headers, SecuritySubject subject) {
        this.delegate = delegate;
        this.stack = stack;
        this.headers = headers;
        this.subject = subject;
    }

    public void run() {
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
        try {
            workContext.setSubject(subject);
            workContext.addHeaders(headers);
            workContext.addCallbackReferences(stack);
            delegate.run();
        } finally {
            workContext.reset();
        }
    }
}
