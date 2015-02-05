package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.ws.generator.WsCallbackBindingGenerator;
import org.fabric3.binding.ws.introspection.WsBindingLoader;
import org.fabric3.binding.ws.introspection.WsBindingPostProcessor;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class MetroIntrospectionProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "MetroIntrospection");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
        compositeBuilder.component(SystemComponentBuilder.newBuilder(WsBindingLoader.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(WsBindingPostProcessor.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(WsCallbackBindingGenerator.class).build());
        return compositeBuilder.build();
    }
}
