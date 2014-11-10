/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.builder.WireDefinitionBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.contribution.ClasspathProcessorRegistryImpl;
import org.fabric3.contribution.ContributionLoaderImpl;
import org.fabric3.contribution.ContributionServiceImpl;
import org.fabric3.contribution.DefaultContributionResolver;
import org.fabric3.contribution.DependencyResolverImpl;
import org.fabric3.contribution.ExtensionMapContentTypeResolver;
import org.fabric3.contribution.ProcessorRegistryImpl;
import org.fabric3.contribution.archive.ArchiveContributionProcessor;
import org.fabric3.contribution.archive.ComponentJavaArtifactIntrospector;
import org.fabric3.contribution.archive.ExplodedArchiveContributionHandler;
import org.fabric3.contribution.archive.JarClasspathProcessor;
import org.fabric3.contribution.archive.ZipContributionHandler;
import org.fabric3.contribution.generator.JavaContributionWireGenerator;
import org.fabric3.contribution.generator.LocationContributionWireGenerator;
import org.fabric3.contribution.introspector.CompositeReferenceIntrospector;
import org.fabric3.contribution.listener.APIImportListener;
import org.fabric3.contribution.manifest.ContributionElementLoader;
import org.fabric3.contribution.manifest.ContributionExportLoader;
import org.fabric3.contribution.manifest.ContributionImport;
import org.fabric3.contribution.manifest.ContributionImportLoader;
import org.fabric3.contribution.manifest.ExtendsLoader;
import org.fabric3.contribution.manifest.JavaExportLoader;
import org.fabric3.contribution.manifest.JavaImportLoader;
import org.fabric3.contribution.manifest.LibraryLoader;
import org.fabric3.contribution.manifest.OSGiManifestHandler;
import org.fabric3.contribution.manifest.ProvidesLoader;
import org.fabric3.contribution.manifest.QNameExportLoader;
import org.fabric3.contribution.manifest.QNameImportLoader;
import org.fabric3.contribution.processor.CompositeContributionProcessor;
import org.fabric3.contribution.processor.CompositeResourceProcessor;
import org.fabric3.contribution.processor.ConfigIndexer;
import org.fabric3.contribution.processor.ConfigProcessor;
import org.fabric3.contribution.processor.DefinitionsProcessor;
import org.fabric3.contribution.processor.DeploymentPlanXmlProcessor;
import org.fabric3.contribution.processor.JavaResourceProcessor;
import org.fabric3.contribution.processor.ProviderResourceProcessor;
import org.fabric3.contribution.processor.SymLinkContributionProcessor;
import org.fabric3.contribution.processor.XmlContributionProcessor;
import org.fabric3.contribution.processor.XmlIndexerRegistryImpl;
import org.fabric3.contribution.processor.XmlProcessorRegistryImpl;
import org.fabric3.contribution.processor.XmlResourceElementLoaderRegistryImpl;
import org.fabric3.contribution.processor.XmlResourceProcessor;
import org.fabric3.contribution.updater.CompositeResourceElementUpdater;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistryImpl;
import org.fabric3.contribution.wire.JavaContributionWire;
import org.fabric3.contribution.wire.JavaContributionWireInstantiator;
import org.fabric3.contribution.wire.LocationContributionWire;
import org.fabric3.contribution.wire.LocationContributionWireInstantiator;
import org.fabric3.contribution.wire.QNameWireInstantiator;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.QNameImport;
import org.oasisopen.sca.Constants;
import static org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder.newBuilder;

/**
 * Provides components for the contribution service.
 */
public class ContributionServiceProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "ContributionServiceComposite");

    @Provides
    public static Composite getComposite() {

        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);

        compositeBuilder.component(newBuilder(ContributionServiceImpl.class).build());

        compositeBuilder.component(newBuilder(ContributionLoaderImpl.class).build());

        compositeBuilder.component(newBuilder(DependencyResolverImpl.class).build());

        compositeBuilder.component(newBuilder(JavaContributionWireGenerator.class).key(JavaContributionWire.class.getName()).build());

        compositeBuilder.component(newBuilder(LocationContributionWireGenerator.class).key(LocationContributionWire.class.getName()).build());

        compositeBuilder.component(newBuilder(ClasspathProcessorRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(JarClasspathProcessor.class).build());

        compositeBuilder.component(newBuilder(ArchiveContributionProcessor.class).build());

        compositeBuilder.component(newBuilder(ZipContributionHandler.class).build());

        compositeBuilder.component(newBuilder(ExplodedArchiveContributionHandler.class).build());

        compositeBuilder.component(newBuilder(XmlResourceProcessor.class).build());

        compositeBuilder.component(newBuilder(CompositeResourceProcessor.class).build());

        compositeBuilder.component(newBuilder(ProviderResourceProcessor.class).build());

        compositeBuilder.component(newBuilder(JavaResourceProcessor.class).build());

        compositeBuilder.component(newBuilder(XmlContributionProcessor.class).build());

        compositeBuilder.component(newBuilder(SymLinkContributionProcessor.class).build());

        compositeBuilder.component(newBuilder(CompositeContributionProcessor.class).build());

        compositeBuilder.component(newBuilder(XmlIndexerRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(XmlProcessorRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(XmlResourceElementLoaderRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(DefinitionsProcessor.class).build());

        compositeBuilder.component(newBuilder(ConfigIndexer.class).build());

        compositeBuilder.component(newBuilder(ConfigProcessor.class).build());

        compositeBuilder.component(newBuilder(DeploymentPlanXmlProcessor.class).build());

        compositeBuilder.component(newBuilder(ContributionElementLoader.class).build());

        compositeBuilder.component(newBuilder(QNameExportLoader.class).key(Constants.SCA_PREFIX + "export").build());

        compositeBuilder.component(newBuilder(QNameImportLoader.class).key(Constants.SCA_PREFIX + "import").build());

        compositeBuilder.component(newBuilder(ContributionImportLoader.class).key(Constants.SCA_PREFIX + "import.contribution").build());

        compositeBuilder.component(newBuilder(ContributionExportLoader.class).key(Constants.SCA_PREFIX + "export.contribution").build());

        compositeBuilder.component(newBuilder(JavaExportLoader.class).key(Constants.SCA_PREFIX + "export.java").build());

        compositeBuilder.component(newBuilder(JavaImportLoader.class).key(Constants.SCA_PREFIX + "import.java").build());

        compositeBuilder.component(newBuilder(ProvidesLoader.class).key(Namespaces.F3_PREFIX + "provides").build());

        compositeBuilder.component(newBuilder(ExtendsLoader.class).key(Namespaces.F3_PREFIX + "extends").build());

        compositeBuilder.component(newBuilder(ContributionWireInstantiatorRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(ProcessorRegistryImpl.class).build());

        compositeBuilder.component(newBuilder(QNameWireInstantiator.class).key(QNameImport.class.getName()).build());

        compositeBuilder.component(newBuilder(JavaContributionWireInstantiator.class).key(JavaImport.class.getName()).build());

        compositeBuilder.component(newBuilder(LocationContributionWireInstantiator.class).key(ContributionImport.class.getName()).build());

        compositeBuilder.component(newBuilder(OSGiManifestHandler.class).build());

        compositeBuilder.component(newBuilder(DefaultContributionResolver.class).key("local").build());

        compositeBuilder.component(newBuilder(ExtensionMapContentTypeResolver.class).build());

        compositeBuilder.component(newBuilder(LibraryLoader.class).key(Namespaces.F3_PREFIX + "library").build());

        compositeBuilder.component(newBuilder(CompositeReferenceIntrospector.class).key(Composite.class.getName()).build());

        compositeBuilder.component(newBuilder(CompositeResourceElementUpdater.class).key(Composite.class.getName()).build());

        compositeBuilder.component(newBuilder(ComponentJavaArtifactIntrospector.class).build());

        compositeBuilder.component(newBuilder(APIImportListener.class).build());

        // reinject the metadata store after runtime bootstrap

        reinjectMetaDataStore(compositeBuilder);

        return compositeBuilder.build();
    }

    private static void reinjectMetaDataStore(CompositeBuilder compositeBuilder) {
        WireDefinitionBuilder wireBuilder = WireDefinitionBuilder.newBuilder();
        compositeBuilder.wire(wireBuilder.source("MetaDataStore/instantiatorRegistry").target("ContributionWireInstantiatorRegistry").build());

        wireBuilder = WireDefinitionBuilder.newBuilder();
        compositeBuilder.wire(wireBuilder.source("MetaDataStore/processorRegistry").target("ProcessorRegistry").build());
    }
}

