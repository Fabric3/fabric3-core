package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.discovery.local.LocalDiscoveryAgent;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class localProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "localDiscoveryExtension");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder builder = CompositeBuilder.newBuilder(QNAME);
        builder.component(SystemComponentBuilder.newBuilder(LocalDiscoveryAgent.class).build());
        builder.deployable();
        return builder.build();
    }
}
