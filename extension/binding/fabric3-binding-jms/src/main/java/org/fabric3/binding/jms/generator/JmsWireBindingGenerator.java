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
package org.fabric3.binding.jms.generator;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fabric3.api.binding.jms.model.ActivationSpec;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.DeliveryMode;
import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.ResponseDefinition;
import org.fabric3.api.binding.jms.model.TransactionType;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.binding.jms.spi.generator.JmsResourceProvisioner;
import org.fabric3.binding.jms.spi.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.spi.provision.JmsWireTargetDefinition;
import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.deployment.generator.wire.WireBindingGenerator;
import org.fabric3.spi.deployment.generator.policy.EffectivePolicy;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalDataTypes;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Binding generator that creates the source and target definitions for JMS endpoint and reference wires.
 */
@EagerInit
public class JmsWireBindingGenerator implements WireBindingGenerator<JmsBindingDefinition> {
    private static final String JAXB = "JAXB";

    private static final QName TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName IMMEDIATE_ONEWAY = new QName(Constants.SCA_NS, "immediateOneWay");
    private static final QName ONEWAY = new QName(Constants.SCA_NS, "oneWay");
    private static final QName NON_PERSISTENT = new QName(org.fabric3.api.Namespaces.F3, "nonPersistent");

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

    public JmsWireSourceDefinition generateSource(LogicalBinding<JmsBindingDefinition> binding,
                                              ServiceContract contract,
                                              List<LogicalOperation> operations,
                                              EffectivePolicy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(operations, policy);
        JmsBindingMetadata metadata = binding.getDefinition().getJmsMetadata().snapshot();

        // set the client id specifier
        String specifier = JmsGeneratorHelper.getSourceSpecifier(binding.getParent().getUri());
        metadata.setClientIdSpecifier(specifier);

        processServiceResponse(metadata, contract);

        generateIntents(binding, metadata);

        List<OperationPayloadTypes> payloadTypes = processPayloadTypes(contract);
        URI uri = binding.getDefinition().getTargetUri();

        List<PhysicalBindingHandlerDefinition> handlers = JmsGeneratorHelper.generateBindingHandlers(info.getDomain(), binding.getDefinition());
        JmsWireSourceDefinition definition;
        if (isJAXB(contract)) {
            definition = new JmsWireSourceDefinition(uri, metadata, payloadTypes, transactionType, handlers, PhysicalDataTypes.JAXB);
        } else {
            definition = new JmsWireSourceDefinition(uri, metadata, payloadTypes, transactionType, handlers);
        }
        if (provisioner != null) {
            provisioner.generateSource(definition);
        }

        setDefaultFactoryConfigurations(metadata, transactionType, specifier);
        processDestinationDefinitions(metadata, false);

        return definition;
    }

    public JmsWireTargetDefinition generateTarget(LogicalBinding<JmsBindingDefinition> binding,
                                              ServiceContract contract,
                                              List<LogicalOperation> operations,
                                              EffectivePolicy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(operations, policy);

        URI uri = binding.getDefinition().getTargetUri();
        JmsBindingMetadata metadata = binding.getDefinition().getJmsMetadata().snapshot();
        processReferenceResponse(metadata, contract);

        List<OperationPayloadTypes> payloadTypes = processPayloadTypes(contract);

        List<PhysicalBindingHandlerDefinition> handlers = JmsGeneratorHelper.generateBindingHandlers(info.getDomain(), binding.getDefinition());
        JmsWireTargetDefinition definition;
        if (isJAXB(contract)) {
            definition = new JmsWireTargetDefinition(uri, metadata, payloadTypes, transactionType, handlers, PhysicalDataTypes.JAXB);
        } else {
            definition = new JmsWireTargetDefinition(uri, metadata, payloadTypes, transactionType, handlers);
        }
        if (provisioner != null) {
            provisioner.generateTarget(definition);
        }

        String specifier = JmsGeneratorHelper.getTargetSpecifier(binding.getParent().getUri());
        setDefaultFactoryConfigurations(metadata, transactionType, specifier);
        processDestinationDefinitions(metadata, true);

        if (contract.getCallbackContract() != null) {
            for (LogicalBinding<?> callbackBinding : binding.getParent().getCallbackBindings()) {
                if (callbackBinding.getDefinition() instanceof JmsBindingDefinition) {
                    JmsBindingDefinition callbackDefinition = (JmsBindingDefinition) callbackBinding.getDefinition();
                    DestinationDefinition callbackDestination = callbackDefinition.getJmsMetadata().getDestination();
                    definition.setCallbackDestination(callbackDestination);
                }
            }
        }
        return definition;
    }

    public JmsWireTargetDefinition generateServiceBindingTarget(LogicalBinding<JmsBindingDefinition> binding,
                                                            ServiceContract contract,
                                                            List<LogicalOperation> operations,
                                                            EffectivePolicy policy) throws GenerationException {
        return generateTarget(binding, contract, operations, policy);
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
            if (!operation.getIntents().contains(ONEWAY)) {
                ResponseDefinition responseDefinition = new ResponseDefinition();
                responseDefinition.setConnectionFactory(metadata.getConnectionFactory());
                DestinationDefinition destinationDefinition = new DestinationDefinition();
                destinationDefinition.setCreate(CreateOption.IF_NOT_EXIST);
                destinationDefinition.setName(metadata.getDestination().getName() + "Response");
                responseDefinition.setDestination(destinationDefinition);
                metadata.setResponse(responseDefinition);
                break;
            }
        }
    }

    /**
     * Verifies a response connection factory destination is provided on a service for request-response MEP.  If not, the request connection factory is used.
     * <p/>
     * Note: a response destination is <strong>not</strong> manufactured as the service must use the response destination set in the JMSReplyTo header of the
     * message request.
     *
     * @param metadata the JMS metadata
     * @param contract the service contract
     * @throws JmsGenerationException if there is an error processing the response
     */
    private void processServiceResponse(JmsBindingMetadata metadata, ServiceContract contract) throws JmsGenerationException {
        if (metadata.isResponse()) {
            if (metadata.getResponse().getActivationSpec() != null) {
                throw new JmsGenerationException("Activation spec not allowed on a service binding response");
            }
            return;
        }
        for (Operation operation : contract.getOperations()) {
            if (!operation.getIntents().contains(ONEWAY)) {
                ResponseDefinition responseDefinition = new ResponseDefinition();
                responseDefinition.setConnectionFactory(metadata.getConnectionFactory());
                metadata.setResponse(responseDefinition);
                break;
            }
        }
    }

    /**
     * Generates intent metadata
     *
     * @param binding  the binding
     * @param metadata the JSM metadata
     */
    private void generateIntents(LogicalBinding<JmsBindingDefinition> binding, JmsBindingMetadata metadata) {
        Set<QName> intents = binding.getDefinition().getIntents();
        if (intents.contains(NON_PERSISTENT)) {
            metadata.getHeaders().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        }
    }

    /**
     * Determines the service transaction type.
     *
     * @param operations the operations defined by the service contract
     * @param policy     the applicable policy
     * @return the transaction type
     */
    private TransactionType getTransactionType(List<LogicalOperation> operations, EffectivePolicy policy) {

        // If any operation has the intent, return that
        for (LogicalOperation operation : operations) {
            for (Intent intent : policy.getIntents(operation)) {
                QName name = intent.getName();
                if (TRANSACTED_ONEWAY.equals(name)) {
                    return TransactionType.GLOBAL;
                } else if (IMMEDIATE_ONEWAY.equals(name)) {
                    return TransactionType.NONE;
                }
            }
        }
        for (Intent intent : policy.getProvidedEndpointIntents()) {
            QName name = intent.getName();
            if (TRANSACTED_ONEWAY.equals(name)) {
                return TransactionType.GLOBAL;
            } else if (IMMEDIATE_ONEWAY.equals(name)) {
                return TransactionType.NONE;
            }
        }
        //no transaction policy specified, use local
        return TransactionType.NONE;
    }

    /**
     * Determines the the payload type to use based on the service contract.
     *
     * @param serviceContract the service contract
     * @return the collection of payload types
     * @throws JmsGenerationException if an error occurs
     */
    private List<OperationPayloadTypes> processPayloadTypes(ServiceContract serviceContract) throws JmsGenerationException {
        List<OperationPayloadTypes> types = new ArrayList<>();
        for (Operation operation : serviceContract.getOperations()) {
            OperationPayloadTypes payloadType = introspector.introspect(operation);
            types.add(payloadType);
        }
        return types;
    }

    private void setDefaultFactoryConfigurations(JmsBindingMetadata metadata, TransactionType trxType, String specifier) {
        // create the connection factory name if one not explicitly given
        ConnectionFactoryDefinition factory = metadata.getConnectionFactory();
        JmsGeneratorHelper.generateDefaultFactoryConfiguration(factory, specifier, trxType);

        ConnectionFactoryDefinition responseFactory = metadata.getResponseConnectionFactory();
        if (responseFactory != null) {
            JmsGeneratorHelper.generateDefaultFactoryConfiguration(responseFactory, specifier + "Response", trxType);
        }
    }

    private void processDestinationDefinitions(JmsBindingMetadata metadata, boolean reference) throws JmsGenerationException {
        DestinationDefinition destination = metadata.getDestination();
        if (destination == null) {
            // create a definition from the activation spec
            ActivationSpec spec = metadata.getActivationSpec();
            if (spec != null) {
                if (reference) {
                    throw new JmsGenerationException("Activation specification not allowed on a reference");
                }
                destination = populateActivationInformation(spec);
                metadata.setDestination(destination);
            }
        }
        DestinationDefinition responseDestination = metadata.getResponseDestination();
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
    private DestinationDefinition populateActivationInformation(ActivationSpec spec) {
        DestinationDefinition destination = new DestinationDefinition();
        destination.setCreate(spec.getCreate());
        destination.setName(spec.getName());
        destination.addProperties(spec.getProperties());
        return destination;
    }

}
