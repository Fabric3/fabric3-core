package org.fabric3.execution.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.invocation.CallbackReference;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextCache;

/**
 * Proxies an executor service to create {@link PropagatingCallable} and {@link PropagatingRunnable} wrappers that propagate the current work context
 * to the thread the submitted callable or runnable is executed on.
 */
public class ExecutorServiceProxy implements ExecutorService {
    private ExecutorService delegate;

    public ExecutorServiceProxy(ExecutorService delegate) {
        this.delegate = delegate;
    }

    public void shutdown() {
        throw new UnsupportedOperationException("Components cannot shutdown the runtime executor service");
    }

    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Components cannot shutdown the runtime executor service");
    }

    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    public <T> Future<T> submit(Callable<T> task) {
        PropagatingCallable<T> callable = createCallable(task);
        return delegate.submit(callable);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        PropagatingRunnable runnable = createRunnable(task);
        return delegate.submit(runnable, result);
    }

    public Future<?> submit(Runnable task) {
        PropagatingRunnable runnable = createRunnable(task);
        return delegate.submit(runnable);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<PropagatingCallable<T>> callables = createCollection(tasks);
        return delegate.invokeAll(callables);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        List<PropagatingCallable<T>> callables = createCollection(tasks);
        return delegate.invokeAll(callables, timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        List<PropagatingCallable<T>> callables = createCollection(tasks);
        return delegate.invokeAny(callables);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        List<PropagatingCallable<T>> callables = createCollection(tasks);
        return delegate.invokeAny(callables, timeout, unit);
    }

    private <T> List<PropagatingCallable<T>> createCollection(Collection<? extends Callable<T>> tasks) {
        List<PropagatingCallable<T>> callables = new ArrayList<PropagatingCallable<T>>(tasks.size());
        for (Callable<T> task : tasks) {
            callables.add(createCallable(task));
        }
        return callables;
    }

    public void execute(Runnable command) {
        PropagatingRunnable runnable = createRunnable(command);
        delegate.execute(runnable);
    }

    private PropagatingRunnable createRunnable(Runnable runnable) {
        WorkContext context = WorkContextCache.getThreadWorkContext();
        List<CallbackReference> stack = context.getCallbackReferences();
        if (stack != null && !stack.isEmpty()) {
            // clone the callstack to avoid multiple threads seeing changes
            stack = new ArrayList<CallbackReference>(stack);
        }
        Map<String, Object> headers = context.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            // clone the headers to avoid multiple threads seeing changes
           headers = new HashMap<String, Object>(headers);
        }
        SecuritySubject subject = context.getSubject();
        return new PropagatingRunnable(runnable, stack, headers, subject);
    }

    private <T> PropagatingCallable<T> createCallable(Callable<T> callable) {
        WorkContext context = WorkContextCache.getThreadWorkContext();
        List<CallbackReference> stack = context.getCallbackReferences();
        if (stack != null && !stack.isEmpty()) {
            // clone the callstack to avoid multiple threads seeing changes
            stack = new ArrayList<CallbackReference>(stack);
        }
        Map<String, Object> headers = context.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            // clone the headers to avoid multiple threads seeing changes
           headers = new HashMap<String, Object>(headers);
        }
        SecuritySubject subject = context.getSubject();
        return new PropagatingCallable<T>(callable, stack, headers, subject);
    }


}
