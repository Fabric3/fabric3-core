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
package org.fabric3.binding.jms.generator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.binding.jms.spi.common.ActivationSpec;
import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.DeliveryMode;
import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.common.ResponseDefinition;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.binding.jms.spi.generator.JmsResourceProvisioner;
import org.fabric3.binding.jms.spi.provision.JmsSourceDefinition;
import org.fabric3.binding.jms.spi.provision.JmsTargetDefinition;
import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;
import org.fabric3.binding.jms.spi.provision.PayloadType;
import org.fabric3.host.Namespaces;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.type.xsd.XSDType;

/**
 * Binding generator that creates the source and target definitions for JMS endpoint and reference wires.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsBindingGenerator implements BindingGenerator<JmsBindingDefinition> {

    private static final QName TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName IMMEDIATE_ONEWAY = new QName(Constants.SCA_NS, "immediateOneWay");
    private static final QName ONEWAY = new QName(Constants.SCA_NS, "oneWay");
    private static final QName NON_PERSISTENT = new QName(Namespaces.F3, "nonPersistent");

    private static final DataType<?> ANY = new XSDType(String.class, new QName(XSDType.XSD_NS, "anyType"));

    private PayloadTypeIntrospector introspector;

    // optional provisioner for host runtimes to receive callbacks
    private JmsResourceProvisioner provisioner;

    public JmsBindingGenerator(@Reference PayloadTypeIntrospector introspector) {
        this.introspector = introspector;
    }

    @Reference(required = false)
    public void setProvisioner(JmsResourceProvisioner provisioner) {
        this.provisioner = provisioner;
    }

    public JmsSourceDefinition generateSource(LogicalBinding<JmsBindingDefinition> binding,
                                              ServiceContract contract,
                                              List<LogicalOperation> operations,
                                              EffectivePolicy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(operations, policy);
        JmsBindingMetadata metadata = binding.getDefinition().getJmsMetadata().snapshot();

        // set the client id specifier
        String specifier = JmsGeneratorHelper.getSourceSpecifier(binding.getParent().getUri());
        metadata.setClientIdSpecifier(specifier);

        validateResponseDestination(metadata, contract);

        generateIntents(binding, metadata);

        List<OperationPayloadTypes> payloadTypes = processPayloadTypes(contract);
        URI uri = binding.getDefinition().getTargetUri();
        JmsSourceDefinition definition = null;
        for (OperationPayloadTypes types : payloadTypes) {
            if (PayloadType.XML == types.getInputType()) {
                // set the source type to string XML
                definition = new JmsSourceDefinition(uri, metadata, payloadTypes, transactionType, ANY);
                break;
            }
        }
        if (definition == null) {
            definition = new JmsSourceDefinition(uri, metadata, payloadTypes, transactionType);
        }
        if (provisioner != null) {
            provisioner.generateSource(definition);
        }

        setDefaultFactoryConfigurations(metadata, transactionType, specifier);
        processDestinationDefinitions(metadata);

        return definition;
    }

    public JmsTargetDefinition generateTarget(LogicalBinding<JmsBindingDefinition> binding,
                                              ServiceContract contract,
                                              List<LogicalOperation> operations,
                                              EffectivePolicy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(operations, policy);

        URI uri = binding.getDefinition().getTargetUri();
        JmsBindingMetadata metadata = binding.getDefinition().getJmsMetadata().snapshot();
        validateResponseDestination(metadata, contract);

        List<OperationPayloadTypes> payloadTypes = processPayloadTypes(contract);

        JmsTargetDefinition definition = null;
        for (OperationPayloadTypes types : payloadTypes) {
            if (PayloadType.XML == types.getInputType()) {
                definition = new JmsTargetDefinition(uri, metadata, payloadTypes, transactionType, ANY);
                break;
            }
        }
        if (definition == null) {
            definition = new JmsTargetDefinition(uri, metadata, payloadTypes, transactionType);
        }
        if (provisioner != null) {
            provisioner.generateTarget(definition);
        }

        String specifier = JmsGeneratorHelper.getTargetSpecifier(binding.getParent().getUri());
        setDefaultFactoryConfigurations(metadata, transactionType, specifier);
        processDestinationDefinitions(metadata);

        return definition;
    }

    public JmsTargetDefinition generateServiceBindingTarget(LogicalBinding<JmsBindingDefinition> binding,
                                                            ServiceContract contract,
                                                            List<LogicalOperation> operations,
                                                            EffectivePolicy policy) throws GenerationException {
        return generateTarget(binding, contract, operations, policy);
    }

    /**
     * Validates a response destination is provided for request-response operations
     *
     * @param metadata the JMS metadata
     * @param contract the service contract
     * @throws GenerationException if a response destination was not provided
     */
    private void validateResponseDestination(JmsBindingMetadata metadata, ServiceContract contract) throws GenerationException {
        if (metadata.isResponse()) {
            return;
        }
        for (Operation operation : contract.getOperations()) {
            if (!operation.getIntents().contains(ONEWAY)) {
                throw new GenerationException("Response destination must be specified for operation " + operation.getName() + " on "
                        + contract.getInterfaceName());
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
        for (Intent intent : policy.getEndpointIntents()) {
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
        List<OperationPayloadTypes> types = new ArrayList<OperationPayloadTypes>();
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

    private void processDestinationDefinitions(JmsBindingMetadata metadata) throws JmsGenerationException {
        DestinationDefinition destination = metadata.getDestination();
        if (destination == null) {
            // create a definition from the activation spec
            ActivationSpec spec = metadata.getActivationSpec();
            if (spec != null) {
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
