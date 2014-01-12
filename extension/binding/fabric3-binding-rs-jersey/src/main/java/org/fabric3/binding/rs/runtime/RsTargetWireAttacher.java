package org.fabric3.binding.rs.runtime;

import java.net.URI;
import java.util.List;

import org.fabric3.binding.rs.runtime.container.RsClientInterceptor;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.rs.provision.RsTargetDefinition;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;

/**
 * Attaches a reference to the RS binding.
 */
public class RsTargetWireAttacher implements TargetWireAttacher<RsTargetDefinition> {
    private ClassLoaderRegistry classLoaderRegistry;

    public RsTargetWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(PhysicalSourceDefinition sourceDefinition, RsTargetDefinition def, Wire wire) throws WiringException {
        ClassLoader targetClassLoader = classLoaderRegistry.getClassLoader(def.getClassLoaderId());
        List<InvocationChain> invocationChains = wire.getInvocationChains();
        URI uri = def.getUri();
        String interfaze = def.getProxyInterface();
        try {
            Class<?> interfaceClass = targetClassLoader.loadClass(interfaze);
            for (InvocationChain chain : invocationChains) {
                PhysicalOperationDefinition operation = chain.getPhysicalOperation();
                String operationName = operation.getName();
                List<String> targetParameterTypes = operation.getTargetParameterTypes();
                Class<?> args[] = new Class<?>[targetParameterTypes.size()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = targetClassLoader.loadClass(targetParameterTypes.get(i));
                }
                chain.addInterceptor(new RsClientInterceptor(operationName, interfaceClass, uri, args));
            }
        } catch (Exception e) {
            throw new WiringException(e);
        }
    }

    public ObjectFactory<?> createObjectFactory(RsTargetDefinition def) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detach(PhysicalSourceDefinition sourceDefinition, RsTargetDefinition def) throws WiringException {
        // no-op
    }

}
