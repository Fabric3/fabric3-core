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
package org.fabric3.binding.jms.runtime;

import java.util.Hashtable;
import java.util.List;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.runtime.resolver.AdministeredObjectResolver;
import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.binding.jms.spi.provision.JmsTargetDefinition;
import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;
import org.fabric3.binding.jms.spi.runtime.JmsConstants;
import org.fabric3.binding.jms.spi.runtime.JmsResolutionException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches the reference end of a wire to a JMS destination.
 *
 * @version $Revision$ $Date$
 */
public class JmsTargetWireAttacher implements TargetWireAttacher<JmsTargetDefinition> {
    private AdministeredObjectResolver resolver;
    private TransactionManager tm;
    private ClassLoaderRegistry classLoaderRegistry;


    public JmsTargetWireAttacher(@Reference AdministeredObjectResolver resolver,
                                 @Reference TransactionManager tm,
                                 @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.resolver = resolver;
        this.tm = tm;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(PhysicalSourceDefinition source, JmsTargetDefinition target, Wire wire) throws WiringException {

        WireConfiguration wireConfiguration = new WireConfiguration();
        ClassLoader targetClassLoader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
        wireConfiguration.setClassloader(targetClassLoader);
        wireConfiguration.setCorrelationScheme(target.getMetadata().getCorrelationScheme());
        wireConfiguration.setTransactionManager(tm);
        wireConfiguration.setTransactionType(target.getTransactionType());

        // resolve the connection factories and destinations for the wire
        resolveAdministeredObjects(target, wireConfiguration);

        List<OperationPayloadTypes> types = target.getPayloadTypes();
        for (InvocationChain chain : wire.getInvocationChains()) {
            // setup operation-specific configuration and create an interceptor
            InterceptorConfiguration configuration = new InterceptorConfiguration(wireConfiguration);
            PhysicalOperationDefinition op = chain.getPhysicalOperation();
            String operationName = op.getName();
            configuration.setOperationName(operationName);
            configuration.setOneWay(op.isOneWay());
            OperationPayloadTypes payloadTypes = resolveOperation(operationName, types);
            configuration.setPayloadType(payloadTypes);
            Interceptor interceptor = new JmsInterceptor(configuration);
            chain.addInterceptor(interceptor);
        }

    }

    public void detach(PhysicalSourceDefinition source, JmsTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(JmsTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private void resolveAdministeredObjects(JmsTargetDefinition target, WireConfiguration wireConfiguration) throws WiringException {
        JmsBindingMetadata metadata = target.getMetadata();
        Hashtable<String, String> env = metadata.getEnv();

        ConnectionFactoryDefinition connectionFactoryDefinition = metadata.getConnectionFactory();
        checkDefaults(target, connectionFactoryDefinition);

        try {
            ConnectionFactory requestConnectionFactory = resolver.resolve(connectionFactoryDefinition, env);
            DestinationDefinition destinationDefinition = metadata.getDestination();
            Destination requestDestination = resolver.resolve(destinationDefinition, requestConnectionFactory, env);
            wireConfiguration.setRequestConnectionFactory(requestConnectionFactory);
            wireConfiguration.setRequestDestination(requestDestination);

            if (metadata.isResponse()) {
                connectionFactoryDefinition = metadata.getResponseConnectionFactory();
                checkDefaults(target, connectionFactoryDefinition);

                ConnectionFactory responseConnectionFactory = resolver.resolve(connectionFactoryDefinition, env);
                destinationDefinition = metadata.getResponseDestination();
                Destination responseDestination = resolver.resolve(destinationDefinition, responseConnectionFactory, env);
                ResponseListener listener = new ResponseListener(responseDestination);
                wireConfiguration.setResponseListener(listener);
            }
        } catch (JmsResolutionException e) {
            throw new WiringException(e);
        }

    }

    private OperationPayloadTypes resolveOperation(String operationName, List<OperationPayloadTypes> payloadTypes) {
        for (OperationPayloadTypes type : payloadTypes) {
            if (type.getName().equals(operationName)) {
                return type;
            }
        }
        // programming error
        throw new AssertionError("Error resolving operation: " + operationName);
    }

    /**
     * Sets default connection factory values if not specified.
     *
     * @param target                      the target definition
     * @param connectionFactoryDefinition the connection factory definition
     */
    private void checkDefaults(JmsTargetDefinition target, ConnectionFactoryDefinition connectionFactoryDefinition) {
        String name = connectionFactoryDefinition.getName();
        if (name == null) {
            if (TransactionType.GLOBAL == target.getTransactionType()) {
                connectionFactoryDefinition.setName(JmsConstants.DEFAULT_XA_CONNECTION_FACTORY);
            } else {
                connectionFactoryDefinition.setName(JmsConstants.DEFAULT_CONNECTION_FACTORY);
            }
        }
    }

}