package org.fabric3.binding.rs.runtime;

import java.net.URI;
import java.util.List;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.rs.provision.RsWireTargetDefinition;
import org.fabric3.binding.rs.runtime.container.RsClientInterceptor;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * Attaches a reference to the RS binding.
 */
@Key("org.fabric3.binding.rs.provision.RsWireTargetDefinition")
public class RsTargetWireAttacher implements TargetWireAttacher<RsWireTargetDefinition> {

    public void attach(PhysicalWireSourceDefinition sourceDefinition, RsWireTargetDefinition def, Wire wire) throws Fabric3Exception {
        List<InvocationChain> invocationChains = wire.getInvocationChains();
        URI uri = def.getUri();
        Class<?> interfaceClass = def.getProxyInterface();
        try {
            for (InvocationChain chain : invocationChains) {
                PhysicalOperationDefinition operation = chain.getPhysicalOperation();
                String operationName = operation.getName();
                List<Class<?>> targetParameterTypes = operation.getTargetParameterTypes();
                chain.addInterceptor(new RsClientInterceptor(operationName, interfaceClass, uri, targetParameterTypes));
            }
        } catch (Exception e) {
            throw new Fabric3Exception(e);
        }
    }

}
