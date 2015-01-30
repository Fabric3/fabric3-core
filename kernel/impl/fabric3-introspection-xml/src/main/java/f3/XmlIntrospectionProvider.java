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
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.introspection.xml.binding.BindingHandlerLoader;
import org.fabric3.introspection.xml.common.ComponentConsumerLoader;
import org.fabric3.introspection.xml.common.ComponentProducerLoader;
import org.fabric3.introspection.xml.common.ComponentReferenceLoader;
import org.fabric3.introspection.xml.common.ComponentServiceLoader;
import org.fabric3.introspection.xml.common.ConfigurationLoader;
import org.fabric3.introspection.xml.common.JavaInterfaceLoader;
import org.fabric3.introspection.xml.common.PropertyLoader;
import org.fabric3.introspection.xml.composite.ChannelLoader;
import org.fabric3.introspection.xml.composite.ComponentLoader;
import org.fabric3.introspection.xml.composite.CompositeLoader;
import org.fabric3.introspection.xml.composite.ImplementationCompositeLoader;
import org.fabric3.introspection.xml.composite.IncludeLoader;
import org.fabric3.introspection.xml.composite.PropertyValueLoader;
import org.fabric3.introspection.xml.composite.WireLoader;
import org.fabric3.introspection.xml.template.BindingTemplatePostProcessor;
import org.fabric3.introspection.xml.template.SystemConfigTemplateParser;
import org.fabric3.introspection.xml.template.TemplateElementLoader;
import org.fabric3.introspection.xml.template.TemplateLoader;
import org.fabric3.introspection.xml.template.TemplateRegistryImpl;
import org.fabric3.introspection.xml.template.TemplatesElementLoader;
import org.fabric3.spi.model.type.system.SystemComponentBuilder;
import org.oasisopen.sca.Constants;
import static org.fabric3.spi.model.type.system.SystemComponentBuilder.newBuilder;

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
        addTemplateLoader(compositeBuilder);

        return compositeBuilder.build();
    }

    private static void addTemplateLoader(CompositeBuilder compositeBuilder) {
        SystemComponentBuilder componentBuilder = newBuilder("TemplateLoader", TemplateLoader.class);
        componentBuilder.key(Constants.SCA_PREFIX + "binding.template");
        componentBuilder.property("expectedType", Binding.class.getName());
        compositeBuilder.component(componentBuilder.build());

        compositeBuilder.component(newBuilder(TemplateElementLoader.class).build());

        compositeBuilder.component(newBuilder(BindingTemplatePostProcessor.class).build());

        compositeBuilder.component(newBuilder(TemplatesElementLoader.class).build());

        compositeBuilder.component(newBuilder(SystemConfigTemplateParser.class).build());
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
        compositeBuilder.component(newBuilder(ChannelLoader.class).build());
        compositeBuilder.component(newBuilder(WireLoader.class).key(Constants.SCA_PREFIX + "wire").build());

        compositeBuilder.component(newBuilder("CompositeLoader", CompositeLoader.class).build());

        compositeBuilder.component(newBuilder(ImplementationCompositeLoader.class).build());
        compositeBuilder.component(newBuilder(BindingHandlerLoader.class).key(Namespaces.F3_PREFIX + "handler").build());
        compositeBuilder.component(newBuilder(ConfigurationLoader.class).key(Constants.SCA_PREFIX + "configuration").build());

    }

    private static void addCommon(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(LoaderRegistryImpl.class).build());
        compositeBuilder.component(newBuilder(DefaultLoaderHelper.class).build());
        compositeBuilder.component(newBuilder(TemplateRegistryImpl.class).build());
    }

}
