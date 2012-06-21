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
*/
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;

/**
 * A contract-based artifact that flows data such as a service, reference, consumer, producer, resource or binding.
 *
 * @version $Rev$ $Date$
 */
public class LogicalInvocable extends LogicalAttachPoint {
    private static final long serialVersionUID = 4182922230894994435L;
    protected ServiceContract serviceContract;
    protected List<LogicalOperation> operations;
    protected List<LogicalOperation> callbackOperations;

    /**
     * Constructor.
     *
     * @param uri      URI of the SCA artifact.
     * @param contract the service contract
     * @param parent   Parent of the SCA artifact.
     */
    protected LogicalInvocable(URI uri, ServiceContract contract, LogicalComponent<?> parent) {
        super(uri, parent);
        createOperations(contract);
        this.serviceContract = contract;
    }

    public List<LogicalOperation> getOperations() {
        return operations;
    }

    public List<LogicalOperation> getCallbackOperations() {
        return callbackOperations;
    }

    /**
     * The effective service contract for this bindable. The effective contract may be set through promotion.
     *
     * @return the effective service contract for this bindable
     */
    public ServiceContract getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the effective service contract for this bindable.
     *
     * @param serviceContract the contract
     */
    public void setServiceContract(ServiceContract serviceContract) {
        this.serviceContract = serviceContract;
        createOperations(serviceContract);
    }

    /**
     * Used to replace operations during a copy.
     *
     * @param operations the new operations
     */
    void overrideOperations(List<LogicalOperation> operations) {
        this.operations = operations;
    }

    /**
     * Instantiates logical operations from a service contract
     *
     * @param contract the contract
     */
    protected final void createOperations(ServiceContract contract) {
        operations = new ArrayList<LogicalOperation>();
        callbackOperations = new ArrayList<LogicalOperation>();
        if (contract != null) {
            // null is a convenience allowed for testing so the logical model does not need to be fully created
            for (Operation operation : contract.getOperations()) {
                operations.add(new LogicalOperation(operation, this));
            }
            ServiceContract callbackContract = contract.getCallbackContract();
            if (callbackContract != null) {
                for (Operation operation : callbackContract.getOperations()) {
                    callbackOperations.add(new LogicalOperation(operation, this));
                }
            }
        }
    }

}