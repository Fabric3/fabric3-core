package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.zeromq.generator.ZeroMQCallbackBindingGenerator;
import org.fabric3.binding.zeromq.generator.ZeroMQConnectionBindingGenerator;
import org.fabric3.binding.zeromq.generator.ZeroMQWireBindingGenerator;
import org.fabric3.binding.zeromq.introspection.ZeroMQBindingLoader;
import org.fabric3.binding.zeromq.introspection.ZeroMQPostProcessor;
import org.fabric3.binding.zeromq.provider.ZeroMQBindingProvider;
import org.fabric3.binding.zeromq.runtime.ZeroMQConnectionSourceAttacher;
import org.fabric3.binding.zeromq.runtime.ZeroMQConnectionTargetAttacher;
import org.fabric3.binding.zeromq.runtime.ZeroMQSourceAttacher;
import org.fabric3.binding.zeromq.runtime.ZeroMQTargetAttacher;
import org.fabric3.binding.zeromq.runtime.ZeroMQTransport;
import org.fabric3.binding.zeromq.runtime.broker.ZeroMQPubSubBrokerImpl;
import org.fabric3.binding.zeromq.runtime.broker.ZeroMQWireBrokerImpl;
import org.fabric3.binding.zeromq.runtime.context.ContextManagerImpl;
import org.fabric3.binding.zeromq.runtime.management.ZeroMQManagementServiceImpl;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class ZeroMQProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "ZeroMQExtension");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQConnectionTargetAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQConnectionSourceAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQSourceAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQTargetAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ContextManagerImpl.class).build());

        SystemComponentBuilder pubSubBuilder = SystemComponentBuilder.newBuilder(ZeroMQPubSubBrokerImpl.class);
        pubSubBuilder.reference("executorService","RuntimeThreadPoolExecutor") ;
        compositeBuilder.component(pubSubBuilder.build());

        SystemComponentBuilder wireBrokerBuilder = SystemComponentBuilder.newBuilder(ZeroMQWireBrokerImpl.class);
        wireBrokerBuilder.reference("executorService","RuntimeThreadPoolExecutor") ;
        compositeBuilder.component(wireBrokerBuilder.build());

        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQTransport.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQManagementServiceImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQBindingLoader.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQPostProcessor.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQWireBindingGenerator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQConnectionBindingGenerator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQCallbackBindingGenerator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ZeroMQBindingProvider.class).build());
        return compositeBuilder.build();
    }
}
