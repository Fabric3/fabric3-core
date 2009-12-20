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
package org.fabric3.binding.jms.generator;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.binding.jms.provision.JmsSourceDefinition;
import org.fabric3.binding.jms.provision.JmsTargetDefinition;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.policy.EffectivePolicy;

/**
 * Binding generator that creates the source and target definitions for JMS endpoint and reference wires.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsBindingGenerator implements BindingGenerator<JmsBindingDefinition> {

    // Transacted one way intent
    private static final QName TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName IMMEDIATE_ONEWAY = new QName(Constants.SCA_NS, "immediateOneWay");
    private static final QName ONEWAY = new QName(Constants.SCA_NS, "oneWay");

    private static final DataType<?> ANY = new XSDType(String.class, new QName(XSDType.XSD_NS, "anyType"));

    private PayloadTypeIntrospector introspector;

    public JmsBindingGenerator(@Reference PayloadTypeIntrospector introspector) {
        this.introspector = introspector;
    }

    public JmsSourceDefinition generateSource(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                              ServiceContract contract,
                                              List<LogicalOperation> operations,
                                              EffectivePolicy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(policy, operations);

        JmsBindingMetadata metadata = logicalBinding.getDefinition().getJmsMetadata();
        validateResponseDestination(metadata, contract);
        Map<String, PayloadType> payloadTypes = processPayloadTypes(contract);
        URI uri = logicalBinding.getDefinition().getTargetUri();
        for (PayloadType payloadType : payloadTypes.values()) {
            if (PayloadType.XML == payloadType) {
                // set the source type to string XML
                return new JmsSourceDefinition(uri, metadata, payloadTypes, transactionType, ANY);
            }
        }
        return new JmsSourceDefinition(uri, metadata, payloadTypes, transactionType);
    }

    public JmsTargetDefinition generateTarget(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                              ServiceContract contract,
                                              List<LogicalOperation> operations,
                                              EffectivePolicy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(policy, operations);

        URI uri = logicalBinding.getDefinition().getTargetUri();
        JmsBindingMetadata metadata = logicalBinding.getDefinition().getJmsMetadata();
        validateResponseDestination(metadata, contract);
        Map<String, PayloadType> payloadTypes = processPayloadTypes(contract);

        // FIXME hack
        for (PayloadType payloadType : payloadTypes.values()) {
            if (PayloadType.XML == payloadType) {
                return new JmsTargetDefinition(uri, metadata, payloadTypes, transactionType, ANY);
            }
        }
        return new JmsTargetDefinition(uri, metadata, payloadTypes, transactionType);
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

    /*
     * Gets the transaction type.
     */
    private TransactionType getTransactionType(EffectivePolicy policy, List<LogicalOperation> operations) {

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
     * @return the collection of payload types keyed by operation name
     * @throws JmsGenerationException if an error occurs
     */
    private Map<String, PayloadType> processPayloadTypes(ServiceContract serviceContract) throws JmsGenerationException {
        Map<String, PayloadType> types = new HashMap<String, PayloadType>();
        for (Operation operation : serviceContract.getOperations()) {
            PayloadType payloadType = introspector.introspect(operation);
            types.put(operation.getName(), payloadType);
        }
        return types;
    }
}
