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
import org.fabric3.spi.util.ClassLoading;

/**
 * Attaches a reference to the RS binding.
 */
@Key("org.fabric3.binding.rs.provision.RsWireTargetDefinition")
public class RsTargetWireAttacher implements TargetWireAttacher<RsWireTargetDefinition> {

    public void attach(PhysicalWireSourceDefinition sourceDefinition, RsWireTargetDefinition def, Wire wire) throws Fabric3Exception {
        ClassLoader targetClassLoader = def.getClassLoader();
        List<InvocationChain> invocationChains = wire.getInvocationChains();
        URI uri = def.getUri();
        Class<?> interfaceClass = def.getProxyInterface();
        try {
            for (InvocationChain chain : invocationChains) {
                PhysicalOperationDefinition operation = chain.getPhysicalOperation();
                String operationName = operation.getName();
                List<String> targetParameterTypes = operation.getTargetParameterTypes();
                Class<?> args[] = new Class<?>[targetParameterTypes.size()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = ClassLoading.loadClass(targetClassLoader, targetParameterTypes.get(i));
                }
                chain.addInterceptor(new RsClientInterceptor(operationName, interfaceClass, uri, args));
            }
        } catch (Exception e) {
            throw new Fabric3Exception(e);
        }
    }

}
