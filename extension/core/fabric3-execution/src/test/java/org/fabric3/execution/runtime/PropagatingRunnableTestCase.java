package org.fabric3.execution.runtime;

import junit.framework.TestCase;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 *
 */
public class PropagatingRunnableTestCase extends TestCase {

    public void testPropagation() throws Exception {
        WorkContext workContext = new WorkContext();
        MockRunnable delegate = new MockRunnable();
        PropagatingRunnable runnable = new PropagatingRunnable(delegate, workContext);
        runnable.run();
    }

    private class MockRunnable implements Runnable {

        public void run() {
            assertNotNull(WorkContextTunnel.getThreadWorkContext());
        }
    }
}
