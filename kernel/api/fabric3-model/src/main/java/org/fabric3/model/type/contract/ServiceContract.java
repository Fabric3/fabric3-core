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
package org.fabric3.model.type.contract;

import java.util.Collections;
import java.util.List;

import org.fabric3.model.type.ModelObject;

/**
 * Base class representing service contract information
 *
 * @version $Rev$ $Date$
 */
public abstract class ServiceContract extends ModelObject {
    private static final long serialVersionUID = 7930416351019873131L;
    protected boolean conversational;
    protected boolean remotable;
    protected String interfaceName;
    protected List<Operation> operations;
    protected ServiceContract callbackContract;

    protected ServiceContract() {
    }

    /**
     * Returns the interface name for the contract
     *
     * @return the interface name for the contract
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Sets the interface name for the contract
     *
     * @param interfaceName the interface name
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Returns true if the service contract is conversational
     *
     * @return true if the service contract is conversational
     */
    public boolean isConversational() {
        return conversational;
    }

    /**
     * Sets if the service contract is conversational
     *
     * @param conversational the conversational attribute
     */
    public void setConversational(boolean conversational) {
        this.conversational = conversational;
    }

    /**
     * Returns true if the contract is remotable.
     *
     * @return the true if the contract is remotable
     */
    public boolean isRemotable() {
        return remotable;
    }

    /**
     * Sets if the contract is remotable
     *
     * @param remotable true if the contract is remotable
     */
    public void setRemotable(boolean remotable) {
        this.remotable = remotable;
    }

    /**
     * Returns the operations for the service contract.
     *
     * @return the operations for the service contract
     */
    public List<Operation> getOperations() {
        if (operations == null) {
            return Collections.emptyList();
        }
        return operations;
    }

    /**
     * Sets the operations for the service contract.
     *
     * @param operations the operations for the service contract
     */
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    /**
     * Returns the callback contract associated with the service contract or null if the service does not have a callback.
     *
     * @return the callback contract or null
     */
    public ServiceContract getCallbackContract() {
        return callbackContract;
    }

    /**
     * Sets the callback contract associated with the service contract.
     *
     * @param callbackContract the callback contract
     */
    public void setCallbackContract(ServiceContract callbackContract) {
        this.callbackContract = callbackContract;
    }

    public abstract String getQualifiedInterfaceName();

    public String toString() {
        if (interfaceName != null) {
            return new StringBuilder().append("ServiceContract[").append(interfaceName).append("]").toString();
        } else {
            return super.toString();
        }

    }
}
