package org.fabric3.binding.rs.model;

import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class RsContextResourceReference extends ResourceReference {

    public RsContextResourceReference(String name, Class<?> type) {
        super(name, new JavaServiceContract(type), false);
    }
}
