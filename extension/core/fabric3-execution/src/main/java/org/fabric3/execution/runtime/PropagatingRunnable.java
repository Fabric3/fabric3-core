package org.fabric3.execution.runtime;

import java.util.List;
import java.util.Map;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * Propagates a work context to the current thread executing the delegate runnable.
 */
public class PropagatingRunnable implements Runnable {
    private Runnable delegate;
    private List<CallFrame> stack;
    private Map<String, Object> headers;
    private SecuritySubject subject;

    public PropagatingRunnable(Runnable delegate, List<CallFrame> stack, Map<String, Object> headers, SecuritySubject subject) {
        this.delegate = delegate;
        this.stack = stack;
        this.headers = headers;
        this.subject = subject;
    }

    public void run() {
        WorkContext workContext = WorkContextTunnel.getAndResetThreadWorkContext();
        workContext.setSubject(subject);
        workContext.addHeaders(headers);
        workContext.addCallFrames(stack);
        delegate.run();
    }
}
