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
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.fabric.container.command.Command;
import org.fabric3.spi.domain.generator.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalComponent;

/**
 * Base functionality for build/dispose component generators.
 */
public abstract class AbstractBuildComponentCommandGenerator<T extends Command> implements CommandGenerator<T> {
    private GeneratorRegistry generatorRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    public AbstractBuildComponentCommandGenerator(GeneratorRegistry generatorRegistry, ClassLoaderRegistry classLoaderRegistry) {
        this.generatorRegistry = generatorRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @SuppressWarnings("unchecked")
    protected PhysicalComponent generateDefinition(LogicalComponent<?> component) throws Fabric3Exception {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        Class<? extends Implementation> type = implementation.getClass();
        ComponentGenerator generator = generatorRegistry.getComponentGenerator(type);
        if (generator == null) {
            throw new Fabric3Exception("Generator not found: " + type.getName());
        }
        PhysicalComponent physicalComponent = generator.generate(component);
        URI uri = component.getUri();
        physicalComponent.setComponentUri(uri);
        URI contributionUri = component.getDefinition().getContributionUri();
        physicalComponent.setContributionUri(contributionUri);
        physicalComponent.setClassLoader(classLoaderRegistry.getClassLoader(contributionUri));
        QName deployable = component.getDeployable();
        physicalComponent.setDeployable(deployable);
        return physicalComponent;
    }

}