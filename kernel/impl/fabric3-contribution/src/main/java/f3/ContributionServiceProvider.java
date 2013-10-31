/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
import org.fabric3.contribution.archive.ExplodedArchiveContributionHandler;
import org.fabric3.contribution.archive.JarClasspathProcessor;
import org.fabric3.contribution.archive.JavaArtifactIntrospectorImpl;
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

        compositeBuilder.component(newBuilder(JavaArtifactIntrospectorImpl.class).build());

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

