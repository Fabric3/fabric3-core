package org.fabric3.binding.rs.runtime;

import java.util.function.Supplier;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.binding.rs.provision.RsContextWireTarget;
import org.fabric3.binding.rs.runtime.container.ContextProxy;
import org.fabric3.spi.container.builder.TargetWireAttacher;

/**
 *
 */
@Key("org.fabric3.binding.rs.provision.RsContextWireTarget")
public class RsContextTargetWireAttacher implements TargetWireAttacher<RsContextWireTarget> {

    public Supplier<?> createSupplier(RsContextWireTarget target) {
        return () -> ContextProxy.INSTANCE;
    }

}
