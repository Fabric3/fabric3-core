package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.rs.generator.ProviderResourceGenerator;
import org.fabric3.binding.rs.generator.RsWireBindingGenerator;
import org.fabric3.binding.rs.introspection.RsBindingLoader;
import org.fabric3.binding.rs.introspection.RsJavaResourceProcessorExtension;
import org.fabric3.binding.rs.introspection.RsPostProcessor;
import org.fabric3.binding.rs.introspection.RsProviderIntrospector;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class RsBindingProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "RsExtension");

       @Provides
       public static Composite getComposite() {
           CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
           compositeBuilder.component(SystemComponentBuilder.newBuilder(RsBindingLoader.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(RsPostProcessor.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(RsProviderIntrospector.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(RsJavaResourceProcessorExtension.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(RsWireBindingGenerator.class).build());
           compositeBuilder.component(SystemComponentBuilder.newBuilder(ProviderResourceGenerator.class).build());
           return compositeBuilder.build();
       }
}
