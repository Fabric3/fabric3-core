/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.generator.component;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.fabric.generator.GeneratorNotFoundException;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Base functionality for (un)build component generators.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractBuildComponentCommandGenerator implements CommandGenerator {
    private GeneratorRegistry generatorRegistry;

    public AbstractBuildComponentCommandGenerator(GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    @SuppressWarnings("unchecked")
    protected PhysicalComponentDefinition generateDefinition(LogicalComponent<?> component) throws GenerationException {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        Class<? extends Implementation> type = implementation.getClass();
        ComponentGenerator generator = generatorRegistry.getComponentGenerator(type);
        if (generator == null) {
            throw new GeneratorNotFoundException(type);
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