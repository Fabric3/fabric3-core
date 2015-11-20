package org.fabric3.binding.rs.provision;

import org.fabric3.spi.model.physical.PhysicalWireTarget;

/**
 *
 */
public class RsContextWireTarget extends PhysicalWireTarget {
    private Class<?> type;

    public RsContextWireTarget(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
