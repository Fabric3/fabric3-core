package org.fabric3.binding.rs.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.rs.provision.RsTargetDefinition;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches a reference to the RS binding.
 *
 * @version $Rev$ $Date$
 */
public class RsTargetWireAttacher implements TargetWireAttacher<RsTargetDefinition> {

    @Reference
    private ClassLoaderRegistry classLoaderRegistry;

    public void attach(PhysicalSourceDefinition sourceDefinition, RsTargetDefinition def, Wire wire) throws WiringException {
        ClassLoader targetClassLoader = classLoaderRegistry.getClassLoader(def.getClassLoaderId());
        List<InvocationChain> invocationChains = wire.getInvocationChains();
        URI uri = def.getUri();
        String intf = def.getProxyInterface();
        try {
            Class<?> intfClass = targetClassLoader.loadClass(intf);
            for (InvocationChain chain : invocationChains) {
                PhysicalOperationDefinition operation = chain.getPhysicalOperation();
                String operName = operation.getName();
                List<String> targetParameterTypes = operation.getTargetParameterTypes();
                Class<?> args[] = new Class<?>[targetParameterTypes.size()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = targetClassLoader.loadClass(targetParameterTypes.get(i));
                }
                chain.addInterceptor(new RsClientInterceptor(operName, intfClass, uri, args));
            }
        } catch (Exception e) {
            throw new WiringException(e);
        }
    }

    public ObjectFactory<?> createObjectFactory(RsTargetDefinition def) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detach(PhysicalSourceDefinition sourceDefinition, RsTargetDefinition def) throws WiringException {
    }

    private static class RsClientInterceptor implements Interceptor {

        private RsClientResponse rsResponse;

        public RsClientInterceptor(String operName, Class<?> intf, URI uri, Class<?>... classes) throws Exception {
            rsResponse = createResponseConfiguration(uri, intf, operName, classes);
        }

        public Message invoke(Message m) {
            Object[] args = (Object[]) m.getBody();
            MessageImpl result = null;
            try {
                result = new MessageImpl();
                result.setBody(rsResponse.build(args));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        public void setNext(Interceptor interceptor) {
        }

        public Interceptor getNext() {
            return null;
        }

        private RsClientResponse createResponseConfiguration(URI uri, Class<?> intf, String operation, Class<?>... args) throws Exception {
            Method m = intf.getMethod(operation, args);
            RsClientResponse cfg = new RsClientResponse(m.getReturnType(), uri);
            cfg = cfg.
                    // Class level
                            withPath(intf.getAnnotation(Path.class)).
                    withProduces(intf.getAnnotation(Produces.class)).
                    withConsumes(intf.getAnnotation(Consumes.class)).
                    // Method level overriding
                            withAction(m.getAnnotation(PUT.class)).
                    withAction(m.getAnnotation(POST.class)).
                    withAction(m.getAnnotation(GET.class)).
                    withPath(m.getAnnotation(Path.class)).
                    withProduces(m.getAnnotation(Produces.class)).
                    withConsumes(m.getAnnotation(Consumes.class))
            ;
            Annotation[][] parameterAnnotations = m.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                cfg.withParam(i, parameterAnnotations[i]);
            }
            return cfg;
        }

    }


}
