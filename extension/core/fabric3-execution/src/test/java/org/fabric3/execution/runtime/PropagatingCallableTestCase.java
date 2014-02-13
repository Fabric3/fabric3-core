package org.fabric3.execution.runtime;

import java.util.concurrent.Callable;

import junit.framework.TestCase;

import org.fabric3.spi.container.invocation.WorkContextCache;

/**
 *
 */
public class PropagatingCallableTestCase extends TestCase {

    public void testPropagation() throws Exception {
        MockCallable delegate = new MockCallable();
        PropagatingCallable<Object> callable = new PropagatingCallable<>(delegate, null, null, null);
        callable.call();
    }

    private class MockCallable implements Callable<Object> {

        public Object call() throws Exception {
            assertNotNull(WorkContextCache.getThreadWorkContext());
            return null;
        }
    }
}
