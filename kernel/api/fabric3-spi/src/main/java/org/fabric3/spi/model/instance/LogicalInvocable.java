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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * A contract-based artifact that flows data such as a service, reference, consumer, producer, resource or binding.
 */
public class LogicalInvocable extends LogicalScaArtifact<LogicalComponent<?>> {
    private static final long serialVersionUID = 4182922230894994435L;
    protected ServiceContract serviceContract;
    protected List<LogicalOperation> operations;
    protected List<LogicalOperation> callbackOperations;

    private URI uri;

    /**
     * Constructor.
     *
     * @param uri      URI of the SCA artifact.
     * @param contract the service contract
     * @param parent   Parent of the SCA artifact.
     */
    protected LogicalInvocable(URI uri, ServiceContract contract, LogicalComponent<?> parent) {
        super(parent);
        this.uri = uri;
        createOperations(contract);
        this.serviceContract = contract;
    }

    /**
     * Returns the artifact uri.
     *
     * @return the artifact uri
     */
    public URI getUri() {
        return uri;
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
     * Instantiates logical operations from a service contract
     *
     * @param contract the contract
     */
    protected final void createOperations(ServiceContract contract) {
        operations = new ArrayList<>();
        callbackOperations = new ArrayList<>();
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