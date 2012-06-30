package org.fabric3.binding.web.runtime.common;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.fabric3.spi.threadpool.ExecutionContext;
import org.fabric3.spi.threadpool.ExecutionContextTunnel;
import org.fabric3.spi.threadpool.LongRunnable;

/**
 * Wraps submitted Runnable instances in a @{link LongRunnable} and delegates to a backing ExecutorService.
 * <p/>
 * Atmosphere performs blocking poll operations on queues and this class is used to avoid issuing stuck thread notifications in the runtime, cf
 * FABRICTHREE-651.
 *
 * @version $Rev$ $Date$
 */
public class LongRunningExecutorService implements ExecutorService {
    private ExecutorService delegate;

    public LongRunningExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    public void shutdown() {
        delegate.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
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
        return delegate.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(new DelegatingRunnable(task), result);
    }

    public Future<?> submit(Runnable task) {
        return delegate.submit(new DelegatingRunnable(task));
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(tasks);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.invokeAll(tasks, timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate.invokeAny(tasks);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(tasks, timeout, unit);
    }

    public void execute(Runnable command) {
        delegate.execute(new DelegatingRunnable(command));
    }

    private class DelegatingRunnable implements LongRunnable {
        private Runnable delegate;

        private DelegatingRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        public void run() {
            ExecutionContext context = ExecutionContextTunnel.getThreadExecutionContext();
            if (context != null) {
                // Fix for FABRICTHREE-651
                context.clear();
            }
            delegate.run();
        }
    }
}
