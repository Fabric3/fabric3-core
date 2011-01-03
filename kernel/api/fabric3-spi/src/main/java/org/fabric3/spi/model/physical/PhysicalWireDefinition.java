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
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Used to provision a wire on a runtime. Contains metadata for attaching the wire to a source transport or component and target transport or
 * component.
 *
 * @version $Rev$ $Date$
 */
public class PhysicalWireDefinition implements Serializable {
    private static final long serialVersionUID = 995196092611674935L;

    private PhysicalSourceDefinition source;
    private PhysicalTargetDefinition target;
    private QName sourceDeployable;
    private QName targetDeployable;

    private final Set<PhysicalOperationDefinition> operations;
    private boolean optimizable;

    public PhysicalWireDefinition(PhysicalSourceDefinition source, PhysicalTargetDefinition target, Set<PhysicalOperationDefinition> operations) {
        this.source = source;
        this.target = target;
        this.operations = operations;
    }

    public PhysicalWireDefinition(PhysicalSourceDefinition source,
                                  QName sourceDeployable,
                                  PhysicalTargetDefinition target,
                                  QName targetDeployable,
                                  Set<PhysicalOperationDefinition> operations) {
        this.source = source;
        this.sourceDeployable = sourceDeployable;
        this.target = target;
        this.operations = operations;
        this.targetDeployable = targetDeployable;
    }

    public QName getSourceDeployable() {
        return sourceDeployable;
    }

    public QName getTargetDeployable() {
        return targetDeployable;
    }

    /**
     * Returns true if the wire can be optimized.
     *
     * @return true if the wire can be optimized
     */
    public boolean isOptimizable() {
        return optimizable;
    }

    /**
     * Sets whether the wire can be optimized.
     *
     * @param optimizable whether the wire can be optimized
     */
    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }

    /**
     * Adds an operation definition.
     *
     * @param operation Operation to be added.
     */
    public void addOperation(PhysicalOperationDefinition operation) {
        operations.add(operation);
    }


    /**
     * Returns the available operations.
     *
     * @return Collection of operations.
     */
    public Set<PhysicalOperationDefinition> getOperations() {
        return operations;
    }

    /**
     * Returns the physical definition for the source side of the wire.
     *
     * @return the physical definition for the source side of the wire
     */
    public PhysicalSourceDefinition getSource() {
        return source;
    }

    /**
     * Returns the physical definition for the target side of the wire.
     *
     * @return the physical definition for the target side of the wire
     */
    public PhysicalTargetDefinition getTarget() {
        return target;
    }

}
