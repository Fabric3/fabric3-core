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
package org.fabric3.binding.jms.generator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.binding.jms.model.ActivationSpec;
import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.binding.jms.model.JmsBinding;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.ResponseDefinition;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.binding.jms.spi.generator.JmsResourceProvisioner;
import org.fabric3.binding.jms.spi.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.spi.provision.JmsWireTargetDefinition;
import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;
import org.fabric3.binding.jms.spi.provision.SessionType;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalDataTypes;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Binding generator that creates the source and target definitions for JMS endpoint and reference wires.
 */
@EagerInit
public class JmsWireBindingGenerator implements WireBindingGenerator<JmsBinding> {
    private static final String JAXB = "JAXB";

    private static final String MANAGED_TRANSACTION = "managedTransaction";
    private static final String MANAGED_TRANSACTION_GLOBAL = "managedTransaction.global";
    private static final String MANAGED_TRANSACTION_LOCAL = "managedTransaction.local";

    private PayloadTypeIntrospector introspector;
    private HostInfo info;

    // optional provisioner for host runtimes to receive callbacks
    private JmsResourceProvisioner provisioner;

    public JmsWireBindingGenerator(@Reference PayloadTypeIntrospector introspector, @Reference HostInfo info) {
        this.introspector = introspector;
        this.info = info;
    }

    @Reference(required = false)
    public void setProvisioner(JmsResourceProvisioner provisioner) {
        this.provisioner = provisioner;
    }

    public JmsWireSourceDefinition generateSource(LogicalBinding<JmsBinding> binding, ServiceContract contract, List<LogicalOperation> operations) throws Fabric3Exception {

        SessionType sessionType = getSessionType(binding.getParent().getParent());
        JmsBindingMetadata metadata = binding.getDefinition().getJmsMetadata().snapshot();

        JmsGeneratorHelper.generateDefaultFactoryConfiguration(metadata.getConnectionFactory(), sessionType);
        if (metadata.getResponseConnectionFactory() != null) {
            JmsGeneratorHelper.generateDefaultFactoryConfiguration(metadata.getResponseConnectionFactory(), sessionType);
        }
        processServiceResponse(metadata, contract);

        List<OperationPayloadTypes> payloadTypes = processPayloadTypes(contract);
        URI uri = binding.getDefinition().getTargetUri();

        List<PhysicalBindingHandlerDefinition> handlers = JmsGeneratorHelper.generateBindingHandlers(info.getDomain(), binding.getDefinition());
        JmsWireSourceDefinition definition;
        if (isJAXB(contract)) {
            definition = new JmsWireSourceDefinition(uri, metadata, payloadTypes, sessionType, handlers, PhysicalDataTypes.JAXB);
        } else {
            definition = new JmsWireSourceDefinition(uri, metadata, payloadTypes, sessionType, handlers);
        }
        if (provisioner != null) {
            provisioner.generateSource(definition);
        }

        processDestinationDefinitions(metadata, false);

        return definition;
    }

    public JmsWireTargetDefinition generateTarget(LogicalBinding<JmsBinding> binding, ServiceContract contract, List<LogicalOperation> operations) throws Fabric3Exception {

        SessionType sessionType = getSessionType(binding.getParent().getParent());

        URI uri = binding.getDefinition().getTargetUri();
        JmsBindingMetadata metadata = binding.getDefinition().getJmsMetadata().snapshot();

        JmsGeneratorHelper.generateDefaultFactoryConfiguration(metadata.getConnectionFactory(), sessionType);
        if (metadata.getResponseConnectionFactory() != null) {
            JmsGeneratorHelper.generateDefaultFactoryConfiguration(metadata.getResponseConnectionFactory(), sessionType);
        }

        processReferenceResponse(metadata, contract);

        List<OperationPayloadTypes> payloadTypes = processPayloadTypes(contract);

        List<PhysicalBindingHandlerDefinition> handlers = JmsGeneratorHelper.generateBindingHandlers(info.getDomain(), binding.getDefinition());
        JmsWireTargetDefinition definition;
        if (isJAXB(contract)) {
            definition = new JmsWireTargetDefinition(uri, metadata, payloadTypes, sessionType, handlers, PhysicalDataTypes.JAXB);
        } else {
            definition = new JmsWireTargetDefinition(uri, metadata, payloadTypes, sessionType, handlers);
        }
        if (provisioner != null) {
            provisioner.generateTarget(definition);
        }

        processDestinationDefinitions(metadata, true);

        if (contract.getCallbackContract() != null) {
            for (LogicalBinding<?> callbackBinding : binding.getParent().getCallbackBindings()) {
                if (callbackBinding.getDefinition() instanceof JmsBinding) {
                    JmsBinding callbackDefinition = (JmsBinding) callbackBinding.getDefinition();
                    Destination callbackDestination = callbackDefinition.getJmsMetadata().getDestination();
                    definition.setCallbackDestination(callbackDestination);
                }
            }
        }
        return definition;
    }

    public JmsWireTargetDefinition generateServiceBindingTarget(LogicalBinding<JmsBinding> binding,
                                                                ServiceContract contract,
                                                                List<LogicalOperation> operations) throws Fabric3Exception {
        return generateTarget(binding, contract, operations);
    }

    private boolean isJAXB(ServiceContract contract) {
        for (Operation operation : contract.getOperations()) {
            if (!operation.getInputTypes().isEmpty() && JAXB.equals(operation.getInputTypes().get(0).getDatabinding())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies a response connection factory destination is provided on a reference for request-response MEP.  If not, the request connection factory is used
     * and a response destination is manufactured by taking the request destination name and appending a "Response" suffix.
     *
     * @param metadata the JMS metadata
     * @param contract the service contract
     */
    private void processReferenceResponse(JmsBindingMetadata metadata, ServiceContract contract) {
        if (metadata.isResponse()) {
            return;
        }
        for (Operation operation : contract.getOperations()) {
            if (!operation.isOneWay()) {
                ResponseDefinition responseDefinition = new ResponseDefinition();
                responseDefinition.setConnectionFactory(metadata.getConnectionFactory());
                Destination destination = new Destination();
                destination.setCreate(CreateOption.IF_NOT_EXIST);
                destination.setName(metadata.getDestination().getName() + "Response");
                responseDefinition.setDestination(destination);
                metadata.setResponse(responseDefinition);
                break;
            }
        }
    }

    /**
     * Verifies a response connection factory destination is provided on a service for request-response MEP.  If not, the request connection factory is used.
     *  Note: a response destination is <strong>not</strong> manufactured as the service must use the response destination set in the JMSReplyTo header of
     * the message request.
     *
     * @param metadata the JMS metadata
     * @param contract the service contract
     * @throws Fabric3Exception if there is an error processing the response
     */
    private void processServiceResponse(JmsBindingMetadata metadata, ServiceContract contract) throws Fabric3Exception {
        if (metadata.isResponse()) {
            if (metadata.getResponse().getActivationSpec() != null) {
                throw new Fabric3Exception("Activation spec not allowed on a service binding response");
            }
            return;
        }
        for (Operation operation : contract.getOperations()) {
            if (!operation.isOneWay()) {
                ResponseDefinition responseDefinition = new ResponseDefinition();
                responseDefinition.setConnectionFactory(metadata.getConnectionFactory());
                metadata.setResponse(responseDefinition);
                break;
            }
        }
    }

    /**
     * Determines the service transaction type.
     *
     * @param component the component
     * @return the transaction type
     */
    private SessionType getSessionType(LogicalComponent<?> component) {
        List<String> policies = component.getDefinition().getComponentType().getPolicies();
        if (policies.contains(MANAGED_TRANSACTION) || policies.contains(MANAGED_TRANSACTION_GLOBAL) || policies.contains(MANAGED_TRANSACTION_LOCAL)) {
            return SessionType.GLOBAL_TRANSACTED;
        }
        return SessionType.AUTO_ACKNOWLEDGE;
    }

    /**
     * Determines the the payload type to use based on the service contract.
     *
     * @param serviceContract the service contract
     * @return the collection of payload types
     * @throws Fabric3Exception if an error occurs
     */
    private List<OperationPayloadTypes> processPayloadTypes(ServiceContract serviceContract) throws Fabric3Exception {
        List<OperationPayloadTypes> types = new ArrayList<>();
        for (Operation operation : serviceContract.getOperations()) {
            OperationPayloadTypes payloadType = introspector.introspect(operation);
            types.add(payloadType);
        }
        return types;
    }

    private void processDestinationDefinitions(JmsBindingMetadata metadata, boolean reference) throws Fabric3Exception {
        Destination destination = metadata.getDestination();
        if (destination == null) {
            // create a definition from the activation spec
            ActivationSpec spec = metadata.getActivationSpec();
            if (spec != null) {
                if (reference) {
                    throw new Fabric3Exception("Activation specification not allowed on a reference");
                }
                destination = populateActivationInformation(spec);
                metadata.setDestination(destination);
            }
        }
        Destination responseDestination = metadata.getResponseDestination();
        ResponseDefinition responseDefinition = metadata.getResponse();
        if (responseDestination == null && responseDefinition != null && responseDefinition.getActivationSpec() != null) {
            ActivationSpec spec = responseDefinition.getActivationSpec();
            responseDestination = populateActivationInformation(spec);
            responseDefinition.setDestination(responseDestination);
        }
    }

    /**
     * Creates a destination definition from an activation spec.
     *
     * @param spec the activation spec
     * @return the definition
     */
    private Destination populateActivationInformation(ActivationSpec spec) {
        Destination destination = new Destination();
        destination.setCreate(spec.getCreate());
        destination.setName(spec.getName());
        destination.addProperties(spec.getProperties());
        return destination;
    }

}
