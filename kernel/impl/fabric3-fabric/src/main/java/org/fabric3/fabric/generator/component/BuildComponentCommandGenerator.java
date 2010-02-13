/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.BuildComponentCommand;
import org.fabric3.fabric.command.UnBuildComponentCommand;
import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.fabric.generator.GeneratorNotFoundException;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Creates a command to build a component on a runtime.
 *
 * @version $Rev$ $Date$
 */
public class BuildComponentCommandGenerator implements CommandGenerator {

    private final GeneratorRegistry generatorRegistry;
    private final int order;

    public BuildComponentCommandGenerator(@Reference GeneratorRegistry generatorRegistry, @Property(name = "order") int order) {
        this.generatorRegistry = generatorRegistry;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public CompensatableCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (!(component instanceof LogicalCompositeComponent) && (component.getState() == LogicalState.NEW || !incremental)) {
            PhysicalComponentDefinition definition = generateDefinition(component);
            return new BuildComponentCommand(definition);
        } else if (!(component instanceof LogicalCompositeComponent) && component.getState() == LogicalState.MARKED) {
            PhysicalComponentDefinition definition = generateDefinition(component);
            return new UnBuildComponentCommand(definition);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private PhysicalComponentDefinition generateDefinition(LogicalComponent<?> component) throws GenerationException {
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
