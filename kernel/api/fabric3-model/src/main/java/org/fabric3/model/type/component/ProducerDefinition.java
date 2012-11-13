/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.model.type.component;

import org.fabric3.model.type.AbstractPolicyAware;
import org.fabric3.model.type.contract.ServiceContract;

/**
 * A component type producer.
 */
public class ProducerDefinition extends BindableDefinition {
    private static final long serialVersionUID = -4222312633353056234L;

    private String name;
    private ComponentType parent;
    private ServiceContract serviceContract;

    /**
     * Constructor.
     *
     * @param name            the producer name
     * @param serviceContract the service contract required by this producer
     */
    public ProducerDefinition(String name, ServiceContract serviceContract) {
        this.name = name;
        this.serviceContract = serviceContract;
    }

    public ProducerDefinition(String name) {
        this(name, null);
    }

    /**
     * Returns the producer name.
     *
     * @return the producer name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parent component type of this producer.
     *
     * @return the parent component type
     */
    public ComponentType getParent() {
        return parent;
    }

    /**
     * Sets the parent component type of this producer.
     *
     * @param parent the parent component type
     */
    public void setParent(ComponentType parent) {
        this.parent = parent;
    }

    /**
     * Returns the service contract required by this producer.
     *
     * @return the service contract required by this producer
     */
    public ServiceContract getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the service contract required by this producer.
     *
     * @param serviceContract the service contract required by this producer
     */
    public void setServiceContract(ServiceContract serviceContract) {
        this.serviceContract = serviceContract;
    }


}