package org.fabric3.runtime.weblogic.work;

import org.fabric3.host.Fabric3Exception;

/**
 * Raised when there is an error initializing the executor service.
 *
 * @version $Rev$ $Date$
 */
public class ExecutorInitException extends Fabric3Exception {
    private static final long serialVersionUID = 1348172706805100180L;

    public ExecutorInitException(Throwable cause) {
        super(cause);
    }
}
