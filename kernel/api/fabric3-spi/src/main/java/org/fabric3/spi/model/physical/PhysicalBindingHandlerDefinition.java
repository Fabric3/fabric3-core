package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.net.URI;

/**
 * Model class for a binding handler configuration.
 *
 * @version $Rev$ $Date$
 */
public class PhysicalBindingHandlerDefinition implements Serializable {
    private static final long serialVersionUID = 5402230919047579812L;

    private URI handlerUri;

    /**
     * Constructor
     *
     * @param handlerUri the component URI of the handler
     */
    public PhysicalBindingHandlerDefinition(URI handlerUri) {
        this.handlerUri = handlerUri;
    }

    /**
     * Returns the component URI of the handler.
     *
     * @return the component URI of the handler
     */
    public URI getHandlerUri() {
        return handlerUri;
    }
}
