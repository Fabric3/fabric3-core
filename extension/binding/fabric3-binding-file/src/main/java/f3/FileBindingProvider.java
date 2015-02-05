package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.file.generator.FileWireBindingGenerator;
import org.fabric3.binding.file.introspection.FileBindingLoader;
import org.fabric3.binding.file.introspection.FileBindingPostProcessor;
import org.fabric3.binding.file.runtime.FileSourceWireAttacher;
import org.fabric3.binding.file.runtime.FileTargetWireAttacher;
import org.fabric3.binding.file.runtime.receiver.ReceiverManagerImpl;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class FileBindingProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "FileBindingExtension");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
        compositeBuilder.component(SystemComponentBuilder.newBuilder(FileBindingLoader.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(FileBindingPostProcessor.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(FileWireBindingGenerator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ReceiverManagerImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(FileSourceWireAttacher.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(FileTargetWireAttacher.class).build());
        return compositeBuilder.build();
    }

}
