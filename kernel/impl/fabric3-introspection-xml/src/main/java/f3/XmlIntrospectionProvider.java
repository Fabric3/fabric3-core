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
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.introspection.xml.binding.BindingHandlerLoader;
import org.fabric3.introspection.xml.binding.SCABindingLoader;
import org.fabric3.introspection.xml.common.ComponentConsumerLoader;
import org.fabric3.introspection.xml.common.ComponentProducerLoader;
import org.fabric3.introspection.xml.common.ComponentReferenceLoader;
import org.fabric3.introspection.xml.common.ComponentServiceLoader;
import org.fabric3.introspection.xml.common.ConfigurationLoader;
import org.fabric3.introspection.xml.common.JavaInterfaceLoader;
import org.fabric3.introspection.xml.common.PropertyLoader;
import org.fabric3.introspection.xml.componentType.ComponentTypeLoader;
import org.fabric3.introspection.xml.composite.ChannelLoader;
import org.fabric3.introspection.xml.composite.ComponentLoader;
import org.fabric3.introspection.xml.composite.CompositeLoader;
import org.fabric3.introspection.xml.composite.CompositeReferenceLoader;
import org.fabric3.introspection.xml.composite.CompositeServiceLoader;
import org.fabric3.introspection.xml.composite.ImplementationCompositeLoader;
import org.fabric3.introspection.xml.composite.IncludeLoader;
import org.fabric3.introspection.xml.composite.PolicySetAttachmentLoader;
import org.fabric3.introspection.xml.composite.PropertyValueLoader;
import org.fabric3.introspection.xml.composite.WireLoader;
import org.fabric3.introspection.xml.definitions.BindingTypeLoader;
import org.fabric3.introspection.xml.definitions.DefinitionsIndexer;
import org.fabric3.introspection.xml.definitions.DefinitionsLoader;
import org.fabric3.introspection.xml.definitions.ExternalAttachmentLoader;
import org.fabric3.introspection.xml.definitions.ImplementationTypeLoader;
import org.fabric3.introspection.xml.definitions.IntentLoader;
import org.fabric3.introspection.xml.definitions.PolicySetLoader;
import org.fabric3.introspection.xml.plan.DeploymentPlanIndexer;
import org.fabric3.introspection.xml.plan.DeploymentPlanProcessor;
import org.fabric3.introspection.xml.template.BindingTemplatePostProcessor;
import org.fabric3.introspection.xml.template.SystemConfigTemplateParser;
import org.fabric3.introspection.xml.template.TemplateElementLoader;
import org.fabric3.introspection.xml.template.TemplateLoader;
import org.fabric3.introspection.xml.template.TemplateRegistryImpl;
import org.fabric3.introspection.xml.template.TemplatesElementLoader;
import org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder;
import org.oasisopen.sca.Constants;
import static org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder.newBuilder;

/**
 * Provides components for XML artifact introspection.
 */
public class XmlIntrospectionProvider {

    private static final QName QNAME = new QName(Namespaces.F3, "XMLIntrospectionComposite");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);

        addCommon(compositeBuilder);
        addCompositeLoader(compositeBuilder);
        addDefinitionsLoader(compositeBuilder);
        addPlanLoader(compositeBuilder);
        addTemplateLoader(compositeBuilder);

        return compositeBuilder.build();
    }

    private static void addTemplateLoader(CompositeBuilder compositeBuilder) {
        SystemComponentDefinitionBuilder componentBuilder = newBuilder("TemplateLoader", TemplateLoader.class);
        componentBuilder.key(Constants.SCA_PREFIX + "binding.template");
        componentBuilder.property("expectedType", BindingDefinition.class.getName());
        compositeBuilder.component(componentBuilder.build());

        compositeBuilder.component(newBuilder(TemplateElementLoader.class).build());

        compositeBuilder.component(newBuilder(BindingTemplatePostProcessor.class).build());

        compositeBuilder.component(newBuilder(TemplatesElementLoader.class).build());

        compositeBuilder.component(newBuilder(SystemConfigTemplateParser.class).build());
    }

    private static void addPlanLoader(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(DeploymentPlanIndexer.class).build());
        compositeBuilder.component(newBuilder(DeploymentPlanProcessor.class).build());
    }

    private static void addDefinitionsLoader(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(DefinitionsLoader.class).build());
        compositeBuilder.component(newBuilder(DefinitionsIndexer.class).build());
        compositeBuilder.component(newBuilder(IntentLoader.class).key(Constants.SCA_PREFIX + "intent").build());
        compositeBuilder.component(newBuilder(PolicySetLoader.class).key(Constants.SCA_PREFIX + "policySet").build());
        compositeBuilder.component(newBuilder(BindingTypeLoader.class).key(Constants.SCA_PREFIX + "bindingType").build());
        compositeBuilder.component(newBuilder(ExternalAttachmentLoader.class).key(Constants.SCA_PREFIX + "externalAttachment").build());
        compositeBuilder.component(newBuilder(ImplementationTypeLoader.class).build());
    }

    private static void addCompositeLoader(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(ComponentReferenceLoader.class).build());
        compositeBuilder.component(newBuilder(ComponentServiceLoader.class).build());
        compositeBuilder.component(newBuilder(ComponentProducerLoader.class).build());
        compositeBuilder.component(newBuilder(ComponentConsumerLoader.class).build());
        compositeBuilder.component(newBuilder(PropertyLoader.class).build());
        compositeBuilder.component(newBuilder(JavaInterfaceLoader.class).key(Constants.SCA_PREFIX + "interface.java").build());
        compositeBuilder.component(newBuilder(PropertyValueLoader.class).build());
        compositeBuilder.component(newBuilder(ComponentLoader.class).build());
        compositeBuilder.component(newBuilder(IncludeLoader.class).build());
        compositeBuilder.component(newBuilder(CompositeReferenceLoader.class).build());
        compositeBuilder.component(newBuilder(CompositeServiceLoader.class).build());
        compositeBuilder.component(newBuilder(ChannelLoader.class).build());
        compositeBuilder.component(newBuilder(WireLoader.class).key(Constants.SCA_PREFIX + "wire").build());

        SystemComponentDefinitionBuilder componentBuilder = newBuilder("CompositeLoader", CompositeLoader.class);
        componentBuilder.reference("service", "CompositeServiceLoader");
        componentBuilder.reference("reference", "CompositeReferenceLoader");
        componentBuilder.reference("property", "PropertyLoader");
        compositeBuilder.component(componentBuilder.build());

        compositeBuilder.component(newBuilder(ImplementationCompositeLoader.class).build());
        compositeBuilder.component(newBuilder(ComponentTypeLoader.class).property("property", "PropertyLoader").build());
        compositeBuilder.component(newBuilder(SCABindingLoader.class).build());
        compositeBuilder.component(newBuilder(BindingHandlerLoader.class).key(Namespaces.F3_PREFIX + "handler").build());
        compositeBuilder.component(newBuilder(ConfigurationLoader.class).key(Constants.SCA_PREFIX + "configuration").build());
        compositeBuilder.component(newBuilder(PolicySetAttachmentLoader.class).build());

    }

    private static void addCommon(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(LoaderRegistryImpl.class).build());
        compositeBuilder.component(newBuilder(DefaultLoaderHelper.class).build());
        compositeBuilder.component(newBuilder(TemplateRegistryImpl.class).build());
    }

}
