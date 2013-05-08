package org.fabric3.execution.runtime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 *
 */
public class ExecutorServiceProxyTestCase extends TestCase {
    private WorkContext workContext;
    private ExecutorServiceProxy executorService;

    public void testInvokeAll() throws Exception {
        MockCallable delegate = new MockCallable();
        executorService.invokeAll(Collections.singletonList(delegate));
    }

    public void testInvokeAllExpiration() throws Exception {
        MockCallable delegate = new MockCallable();
        executorService.invokeAll(Collections.singletonList(delegate), 100, TimeUnit.SECONDS);
    }

    public void testInvokeAny() throws Exception {
        MockCallable delegate = new MockCallable();
        executorService.invokeAny(Collections.singletonList(delegate));
    }

    public void testInvokeAnyExpiration() throws Exception {
        MockCallable delegate = new MockCallable();
        executorService.invokeAny(Collections.singletonList(delegate), 100, TimeUnit.SECONDS);
    }

    public void testSubmitRunnable() throws Exception {
        MockRunnable delegate = new MockRunnable();
        executorService.submit(delegate);
    }

    public void testSubmitCallable() throws Exception {
        MockCallable delegate = new MockCallable();
        executorService.submit(delegate);
    }

    public void testSubmitResult() throws Exception {
        MockRunnable delegate = new MockRunnable();
        executorService.submit(delegate, null);
    }

    protected void setUp() throws Exception {
        super.setUp();
        workContext = new WorkContext();
        executorService = new ExecutorServiceProxy(new MockExecutorService());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private class MockCallable implements Callable<Object> {

        public Object call() throws Exception {
            WorkContext propagated = WorkContextTunnel.getThreadWorkContext();
            assertNotNull(propagated);
            assertFalse(workContext.equals(propagated));
            return null;
        }
    }

    private class MockRunnable implements Runnable {

        public void run() {
            WorkContext propagated = WorkContextTunnel.getThreadWorkContext();
            assertNotNull(propagated);
            assertFalse(workContext.equals(propagated));
        }
    }

    private class MockExecutorService implements ExecutorService {

        public void shutdown() {

        }

        public List<Runnable> shutdownNow() {
            return null;
        }

        public boolean isShutdown() {
            return false;
        }

        public boolean isTerminated() {
            return false;
        }

        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return false;
        }

        public <T> Future<T> submit(Callable<T> task) {
            try {
                task.call();
                return null;
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        public <T> Future<T> submit(Runnable task, T result) {
            task.run();
            return null;
        }

        public Future<?> submit(Runnable task) {
            task.run();
            return null;
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            for (Callable<T> task : tasks) {
                try {
                    task.call();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
            return Collections.emptyList();
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return invokeAll(tasks);
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            invokeAll(tasks);
            return null;
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            invokeAll(tasks);
            return null;
        }

        public void execute(Runnable command) {
            command.run();
        }
    }
}
