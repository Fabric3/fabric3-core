package org.fabric3.execution.runtime;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * Propagates a work context to the current thread executing the delegate callable.
 */
public class PropagatingCallable<T> implements Callable<T> {
    private Callable<T> delegate;
    private List<CallFrame> stack;
    private Map<String, Object> headers;
    private SecuritySubject subject;

    public PropagatingCallable(Callable<T> delegate, List<CallFrame> stack, Map<String, Object> headers, SecuritySubject subject) {
        this.delegate = delegate;
        this.stack = stack;
        this.headers = headers;
        this.subject = subject;
    }

    public T call() throws Exception {
        WorkContext workContext = WorkContextTunnel.getAndResetThreadWorkContext();
        workContext.setSubject(subject);
        workContext.addHeaders(headers);
        workContext.addCallFrames(stack);
        return delegate.call();
    }
}
