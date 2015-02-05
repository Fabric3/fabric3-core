package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.rs.runtime.RsSourceWireAttacher;
import org.fabric3.binding.rs.runtime.RsTargetWireAttacher;
import org.fabric3.binding.rs.runtime.builder.ProviderBuilder;
import org.fabric3.binding.rs.runtime.bytecode.ProviderGeneratorImpl;
import org.fabric3.binding.rs.runtime.container.RsContainerManagerImpl;
import org.fabric3.binding.rs.runtime.provider.NameBindingFilterProviderImpl;
import org.fabric3.binding.rs.runtime.provider.ProviderRegistryImpl;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class JerseyProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "RsJerseyExtension");

       @Provides
       public static Composite getComposite() {
           CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
           compositeBuilder.component(SystemComponentBuilder.newBuilder(RsContainerManagerImpl.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(ProviderRegistryImpl.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(ProviderGeneratorImpl.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(NameBindingFilterProviderImpl.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(RsSourceWireAttacher.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(RsTargetWireAttacher.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(ProviderBuilder.class).build());
           return compositeBuilder.build();
       }
}
