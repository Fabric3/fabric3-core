package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.activemq.broker.BrokerEngine;
import org.fabric3.binding.activemq.provider.ActiveMQConnectionFactoryConfigurationParser;
import org.fabric3.binding.activemq.provider.ActiveMQConnectionFactoryCreator;
import org.fabric3.binding.activemq.provider.ActiveMQDefaultConnectionFactoryBuilder;
import org.fabric3.binding.activemq.provider.BrokerHelperImpl;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class ActiveMqProvider {

    private static final QName QNAME = new QName(Namespaces.F3, "ActiveMQExtension");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
        compositeBuilder.component(SystemComponentBuilder.newBuilder(BrokerEngine.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(BrokerHelperImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ActiveMQConnectionFactoryCreator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ActiveMQConnectionFactoryConfigurationParser.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ActiveMQDefaultConnectionFactoryBuilder.class).build());
        return compositeBuilder.build();
    }
}
