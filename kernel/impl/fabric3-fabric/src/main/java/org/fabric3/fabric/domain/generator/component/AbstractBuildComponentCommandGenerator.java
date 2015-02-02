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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.generator.component;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.fabric.domain.generator.CommandGenerator;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Base functionality for build/dispose component generators.
 */
public abstract class AbstractBuildComponentCommandGenerator<T extends Command> implements CommandGenerator<T> {
    private GeneratorRegistry generatorRegistry;

    public AbstractBuildComponentCommandGenerator(GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    @SuppressWarnings("unchecked")
    protected PhysicalComponentDefinition generateDefinition(LogicalComponent<?> component) throws Fabric3Exception {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        Class<? extends Implementation> type = implementation.getClass();
        ComponentGenerator generator = generatorRegistry.getComponentGenerator(type);
        if (generator == null) {
            throw new Fabric3Exception("Generator not found: " + type.getName());
        }
        PhysicalComponentDefinition definition = generator.generate(component);
        URI uri = component.getUri();
        definition.setComponentUri(uri);
        definition.setClassLoaderId(component.getDefinition().getContributionUri());
        QName deployable = component.getDeployable();
        definition.setDeployable(deployable);
        return definition;
    }

}