package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.hazelcast.discovery.HazelcastAgent;
import org.fabric3.hazelcast.impl.HazelcastServiceImpl;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class HazelcastProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "HazelcastExtension");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder builder = CompositeBuilder.newBuilder(QNAME);
        builder.component(SystemComponentBuilder.newBuilder(HazelcastServiceImpl.class).build());
        builder.component(SystemComponentBuilder.newBuilder(HazelcastAgent.class).build());
        builder.deployable();
        return builder.build();
    }
}
