package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.binding.ws.metro.contribution.MetroContributionServiceListener;
import org.fabric3.binding.ws.metro.generator.MetroWireBindingGenerator;
import org.fabric3.binding.ws.metro.generator.java.EndpointSynthesizerImpl;
import org.fabric3.binding.ws.metro.generator.java.JavaGeneratorDelegate;
import org.fabric3.binding.ws.metro.generator.java.codegen.InterfaceGeneratorImpl;
import org.fabric3.binding.ws.metro.generator.resolver.TargetUrlResolverImpl;
import org.fabric3.binding.ws.metro.runtime.core.ArtifactCacheImpl;
import org.fabric3.binding.ws.metro.runtime.core.EndpointServiceImpl;
import org.fabric3.binding.ws.metro.runtime.wire.MetroJavaSourceWireAttacher;
import org.fabric3.binding.ws.metro.runtime.wire.MetroJavaTargetWireAttacher;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

/**
 *
 */
public class MetroBaseProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "MetroBase");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
        compositeBuilder.component(SystemComponentBuilder.newBuilder(ArtifactCacheImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(EndpointSynthesizerImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(TargetUrlResolverImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(InterfaceGeneratorImpl.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(MetroWireBindingGenerator.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(JavaGeneratorDelegate.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(MetroContributionServiceListener.class).build());
        compositeBuilder.component(SystemComponentBuilder.newBuilder(MetroJavaSourceWireAttacher.class).build());

        SystemComponentBuilder targetBuilder = SystemComponentBuilder.newBuilder(MetroJavaTargetWireAttacher.class);
        targetBuilder.reference("executorService", "RuntimeThreadPoolExecutor");
        compositeBuilder.component(targetBuilder.build());

        SystemComponentBuilder endpointBuilder = SystemComponentBuilder.newBuilder(EndpointServiceImpl.class);
        endpointBuilder.reference("executorService", "RuntimeThreadPoolExecutor");
        compositeBuilder.component(endpointBuilder.build());
        return compositeBuilder.build();
    }
}
