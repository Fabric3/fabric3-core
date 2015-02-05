package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.jms.builder.ConnectionFactoryBuilder;
import org.fabric3.binding.jms.builder.JmsChannelBindingBuilder;
import org.fabric3.binding.jms.generator.ConnectionFactoryResourceGenerator;
import org.fabric3.binding.jms.generator.JmsConnectionBindingGenerator;
import org.fabric3.binding.jms.generator.JmsWireBindingGenerator;
import org.fabric3.binding.jms.generator.PayloadTypeIntrospectorImpl;
import org.fabric3.binding.jms.introspection.ConnectionFactoryResourceLoader;
import org.fabric3.binding.jms.introspection.JmsBindingLoader;
import org.fabric3.binding.jms.introspection.JmsBindingPostProcessor;
import org.fabric3.binding.jms.runtime.JmsConnectionSourceAttacher;
import org.fabric3.binding.jms.runtime.JmsConnectionTargetAttacher;
import org.fabric3.binding.jms.runtime.JmsSourceWireAttacher;
import org.fabric3.binding.jms.runtime.JmsTargetWireAttacher;
import org.fabric3.binding.jms.runtime.connection.ConfigurationBuilder;
import org.fabric3.binding.jms.runtime.connection.ConnectionFactoryCreatorRegistryImpl;
import org.fabric3.binding.jms.runtime.container.MessageContainerFactoryImpl;
import org.fabric3.binding.jms.runtime.container.MessageContainerManagerImpl;
import org.fabric3.binding.jms.runtime.jndi.JndiAdministeredObjectResolver;
import org.fabric3.binding.jms.runtime.jndi.JndiClassLoaderUpdater;
import org.fabric3.binding.jms.runtime.resolver.AdministeredObjectResolverImpl;
import org.fabric3.binding.jms.runtime.resolver.connectionfactory.AlwaysConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.resolver.connectionfactory.IfNotExistConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.resolver.connectionfactory.NeverConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.resolver.destination.AlwaysDestinationStrategy;
import org.fabric3.binding.jms.runtime.resolver.destination.IfNotExistDestinationStrategy;
import org.fabric3.binding.jms.runtime.resolver.destination.NeverDestinationStrategy;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class JmsBindingProvider {

    private static final QName QNAME = new QName(Namespaces.F3, "JmsBindingExtension");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
        SystemComponentBuilder builder = SystemComponentBuilder.newBuilder(MessageContainerFactoryImpl.class);
        builder.reference("executorService", "RuntimeThreadPoolExecutor");
        compositeBuilder.component(builder.build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(MessageContainerManagerImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JmsSourceWireAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JmsTargetWireAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JmsConnectionSourceAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JmsConnectionTargetAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(AdministeredObjectResolverImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(AlwaysDestinationStrategy.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(NeverDestinationStrategy.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(IfNotExistDestinationStrategy.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(AlwaysConnectionFactoryStrategy.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(NeverConnectionFactoryStrategy.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(IfNotExistConnectionFactoryStrategy.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JndiAdministeredObjectResolver.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JndiClassLoaderUpdater.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ConnectionFactoryCreatorRegistryImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ConfigurationBuilder.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JmsChannelBindingBuilder.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ConnectionFactoryBuilder.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JmsBindingLoader.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JmsWireBindingGenerator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JmsBindingPostProcessor.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JmsConnectionBindingGenerator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(PayloadTypeIntrospectorImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ConnectionFactoryResourceGenerator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ConnectionFactoryResourceLoader.class).build());
        return compositeBuilder.build();

    }
}
