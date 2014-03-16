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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.container.builder.component;

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;

/**
 * Attaches and detaches a wire to/from a target component or transport binding.
 */
public interface TargetWireAttacher<PTD extends PhysicalWireTargetDefinition> {
    /**
     * Attaches a wire to a target component or outgoing transport binding.
     *
     * @param source metadata for performing the attach
     * @param target metadata for performing the attach
     * @param wire   the wire
     * @throws ContainerException if an exception occurs during the attach operation
     */
    void attach(PhysicalWireSourceDefinition source, PTD target, Wire wire) throws ContainerException;

    /**
     * Detaches a wire from a target component or outgoing transport binding.
     *
     * @param source metadata for performing the attach
     * @param target metadata for performing the attach
     * @throws ContainerException if an exception occurs during the detach operation
     */
    void detach(PhysicalWireSourceDefinition source, PTD target) throws ContainerException;

    /**
     * Create an ObjectFactory that returns a direct target instance.
     *
     * @param target metadata for performing the attach
     * @return an ObjectFactory that returns the target instance
     * @throws ContainerException if an exception occurs during the attach operation
     */
    ObjectFactory<?> createObjectFactory(PTD target) throws ContainerException;

}
