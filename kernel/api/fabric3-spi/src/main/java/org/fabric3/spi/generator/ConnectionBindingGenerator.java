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
package org.fabric3.spi.generator;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 * Generates {@link PhysicalConnectionSourceDefinition}s and {@link PhysicalConnectionTargetDefinition}s for resolved connection bindings.
 *
 * @version $Rev$ $Date$
 */
public interface ConnectionBindingGenerator<BD extends BindingDefinition> {

    /**
     * Generates metadata used to attach an event connection to a transport that is the source of an event stream. The event connection may flow to a
     * channel or consumer.
     *
     * @param producerBinding the binding
     * @return the connection metadata
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalConnectionSourceDefinition generateConnectionSource(LogicalBinding<BD> producerBinding) throws GenerationException;

    /**
     * Generates metadata used to attach an event connection to a transport that is the target of an event stream. The source of the connection may be
     * a channel or producer.
     *
     * @param binding the binding
     * @return the connection metadata
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalBinding<BD> binding) throws GenerationException;


}