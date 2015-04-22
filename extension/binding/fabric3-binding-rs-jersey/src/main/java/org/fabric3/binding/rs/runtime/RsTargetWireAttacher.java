package org.fabric3.binding.rs.runtime;

import java.net.URI;
import java.util.List;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.rs.provision.RsWireTarget;
import org.fabric3.binding.rs.runtime.container.RsClientInterceptor;
import org.fabric3.spi.container.builder.TargetWireAttacher;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireSource;

/**
 * Attaches a reference to the RS binding.
 */
@Key("org.fabric3.binding.rs.provision.RsWireTarget")
public class RsTargetWireAttacher implements TargetWireAttacher<RsWireTarget> {

    public void attach(PhysicalWireSource sourceDefinition, RsWireTarget target, Wire wire) throws Fabric3Exception {
        List<InvocationChain> invocationChains = wire.getInvocationChains();
        URI uri = target.getUri();
        Class<?> interfaceClass = target.getProxyInterface();
        try {
            for (InvocationChain chain : invocationChains) {
                PhysicalOperation operation = chain.getPhysicalOperation();
                String operationName = operation.getName();
                List<Class<?>> targetParameterTypes = operation.getTargetParameterTypes();
                chain.addInterceptor(new RsClientInterceptor(operationName, interfaceClass, uri, targetParameterTypes));
            }
        } catch (Exception e) {
            throw new Fabric3Exception(e);
        }
    }

}
