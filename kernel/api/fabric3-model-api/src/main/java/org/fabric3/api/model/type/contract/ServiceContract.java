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
package org.fabric3.api.model.type.contract;

import java.util.Collections;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;

/**
 * Base class representing service contract information.
 */
public abstract class ServiceContract extends ModelObject<ModelObject> {
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
        for (Operation operation : operations) {
            operation.setParent(this);
        }
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
        if (callbackContract != null) {
            callbackContract.setParent(this);
        }
    }

    public abstract String getQualifiedInterfaceName();

    public Class<?> getInterfaceClass() {
        return null;
    }

    public String toString() {
        if (interfaceName != null) {
            return "ServiceContract[" + interfaceName + "]";
        } else {
            return super.toString();
        }

    }
}
