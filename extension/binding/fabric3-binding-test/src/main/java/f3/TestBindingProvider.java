package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.test.BindingChannelImpl;
import org.fabric3.binding.test.TestBindingLoader;
import org.fabric3.binding.test.TestBindingSourceWireAttacher;
import org.fabric3.binding.test.TestBindingTargetWireAttacher;
import org.fabric3.binding.test.TestWireBindingGenerator;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class TestBindingProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "TestBindingExtension");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
        compositeBuilder.component(SystemComponentBuilder.newBuilder(BindingChannelImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(TestBindingLoader.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(TestWireBindingGenerator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(TestBindingSourceWireAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(TestBindingTargetWireAttacher.class).build());
        return compositeBuilder.build();
    }
}
