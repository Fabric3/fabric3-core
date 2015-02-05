package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.ws.metro.MetroBinding;
import org.fabric3.binding.ws.metro.util.ClassDefinerImpl;
import org.fabric3.binding.ws.metro.util.ClassLoaderUpdaterImpl;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class MetroConfigProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "MetroConfigBinding");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ClassDefinerImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ClassLoaderUpdaterImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(MetroBinding.class).build());
        return compositeBuilder.build();
    }
}
