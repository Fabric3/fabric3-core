package org.fabric3.execution.runtime;

import java.util.concurrent.Callable;

import junit.framework.TestCase;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * @version $Rev$ $Date$
 */
public class PropagatingCallableTestCase extends TestCase {

    public void testPropagation() throws Exception {
        WorkContext workContext = new WorkContext();
        MockCallable delegate = new MockCallable();
        PropagatingCallable<Object> callable = new PropagatingCallable<Object>(delegate, workContext);
        callable.call();
    }

    private class MockCallable implements Callable<Object> {

        public Object call() throws Exception {
            assertNotNull(WorkContextTunnel.getThreadWorkContext());
            return null;
        }
    }
}
