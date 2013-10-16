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
package org.fabric3.binding.jms.spi.provision;

import java.net.URI;
import java.util.List;

import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 * Generated metadata used for attaching a service endpoint to a JMS destination.
 */
public class JmsTargetDefinition extends PhysicalTargetDefinition {
    private static final long serialVersionUID = -151189038434425132L;
    private JmsBindingMetadata metadata;
    private TransactionType transactionType;
    private List<OperationPayloadTypes> payloadTypes;
    private DestinationDefinition callbackDestination;
    private List<PhysicalBindingHandlerDefinition> handlers;

    /**
     * Constructor.
     *
     * @param uri             the service URI
     * @param metadata        metadata used to create a JMS message producer.
     * @param payloadTypes    the JMS payload types
     * @param transactionType the transaction type
     * @param handlers        binding handlers to be engaged for the service
     */
    public JmsTargetDefinition(URI uri,
                               JmsBindingMetadata metadata,
                               List<OperationPayloadTypes> payloadTypes,
                               TransactionType transactionType,
                               List<PhysicalBindingHandlerDefinition> handlers) {
        this.metadata = metadata;
        this.transactionType = transactionType;
        this.payloadTypes = payloadTypes;
        this.handlers = handlers;
        setUri(uri);
    }

    /**
     * Constructor that defines an alternative set of supported data types.
     *
     * @param uri             the service URI
     * @param metadata        metadata used to create a JMS message producer.
     * @param payloadTypes    the JMS payload types
     * @param transactionType the transaction type
     * @param handlers        binding handlers to be engaged for the service
     * @param types           the allowable datatypes. For example, this may be used to constrain a source type to string XML
     */
    public JmsTargetDefinition(URI uri,
                               JmsBindingMetadata metadata,
                               List<OperationPayloadTypes> payloadTypes,
                               TransactionType transactionType,
                               List<PhysicalBindingHandlerDefinition> handlers,
                               DataType<?>... types) {
        super(types);
        this.metadata = metadata;
        this.transactionType = transactionType;
        this.payloadTypes = payloadTypes;
        this.handlers = handlers;
        setUri(uri);
    }

    public JmsBindingMetadata getMetadata() {
        return metadata;
    }

    public List<OperationPayloadTypes> getPayloadTypes() {
        return payloadTypes;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setCallbackDestination(DestinationDefinition definition) {
        this.callbackDestination = definition;
    }

    public DestinationDefinition getCallbackDestination() {
        return callbackDestination;
    }

    public List<PhysicalBindingHandlerDefinition> getHandlers() {
        return handlers;
    }
}
