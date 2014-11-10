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
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.plugin.contribution.PluginContributionProcessor;
import org.fabric3.plugin.contribution.ProjectClasspathProcessor;
import org.fabric3.plugin.test.WireHolderImpl;
import org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder;

/**
 *
 */
public class FabricProvider {
    private static final QName QNAME = new QName(Namespaces.F3, "PluginExtensionComposite");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);
        compositeBuilder.component(SystemComponentDefinitionBuilder.newBuilder(WireHolderImpl.class).build());
        compositeBuilder.component(SystemComponentDefinitionBuilder.newBuilder(ProjectClasspathProcessor.class).build());
        compositeBuilder.component(SystemComponentDefinitionBuilder.newBuilder(PluginContributionProcessor.class).build());
        return compositeBuilder.build();
    }
}
