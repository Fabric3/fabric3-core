/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.generator;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.binding.zeromq.provision.ZeroMQWireSourceDefinition;
import org.fabric3.binding.zeromq.provision.ZeroMQWireTargetDefinition;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.deployment.generator.binding.BindingGenerator;
import org.fabric3.spi.deployment.generator.policy.EffectivePolicy;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class ZeroMQBindingGenerator implements BindingGenerator<ZeroMQBindingDefinition> {
    private static final QName ONEWAY = new QName(Constants.SCA_NS, "oneWay");
    private static final String TARGET_URI = "targetUri";

    public ZeroMQWireSourceDefinition generateSource(LogicalBinding<ZeroMQBindingDefinition> binding,
                                                 ServiceContract contract,
                                                 List<LogicalOperation> operations,
                                                 EffectivePolicy policy) throws GenerationException {
        ZeroMQMetadata metadata = binding.getDefinition().getZeroMQMetadata();
        if (binding.isCallback()) {
            URI uri = URI.create("zmq://" + contract.getInterfaceName());
            return new ZeroMQWireSourceDefinition(uri, metadata);
        } else {
            return new ZeroMQWireSourceDefinition(metadata);
        }
    }

    public ZeroMQWireTargetDefinition generateTarget(LogicalBinding<ZeroMQBindingDefinition> binding,
                                                 ServiceContract contract,
                                                 List<LogicalOperation> operations,
                                                 EffectivePolicy policy) throws GenerationException {
        validateServiceContract(contract);
        ZeroMQMetadata metadata = binding.getDefinition().getZeroMQMetadata();

        if (binding.isCallback()) {
            URI targetUri = URI.create("zmq://" + contract.getInterfaceName());
            return new ZeroMQWireTargetDefinition(targetUri, metadata);
        }
        URI targetUri;
        // If this is an undeployment, use the previously calculated target URI. This must be done since the target component may no longer
        // be in the domain if it has been undeployed from another zone.
        if (LogicalState.MARKED == binding.getState()) {
            targetUri = binding.getMetadata(TARGET_URI, URI.class);
        } else {
            targetUri = parseTargetUri(binding);
            if (targetUri != null) {
                binding.addMetadata(TARGET_URI, targetUri);
            }
        }
        return generateTarget(contract, targetUri, metadata);
    }

    public ZeroMQWireTargetDefinition generateServiceBindingTarget(LogicalBinding<ZeroMQBindingDefinition> binding,
                                                               ServiceContract contract,
                                                               List<LogicalOperation> operations,
                                                               EffectivePolicy policy) throws GenerationException {
        URI targetUri = binding.getParent().getUri();
        ZeroMQMetadata metadata = binding.getDefinition().getZeroMQMetadata();
        return generateTarget(contract, targetUri, metadata);
    }

    private ZeroMQWireTargetDefinition generateTarget(ServiceContract contract, URI targetUri, ZeroMQMetadata metadata) {
        boolean hasCallback = contract.getCallbackContract() != null;
        if (hasCallback) {
            URI callbackUri = URI.create("zmq://" + contract.getCallbackContract().getInterfaceName());
            return new ZeroMQWireTargetDefinition(targetUri, callbackUri, metadata);
        }
        return new ZeroMQWireTargetDefinition(targetUri, metadata);
    }

    /**
     * Parses the target URI. May return null if the target is not set and addresses are explicitly configured.
     *
     * @param binding the binding
     * @return the URI or null
     * @throws GenerationException if there is a parsing error
     */
    private URI parseTargetUri(LogicalBinding<ZeroMQBindingDefinition> binding) throws GenerationException {
        URI bindingTargetUri = binding.getDefinition().getTargetUri();
        if (bindingTargetUri == null) {
            // create a synthetic name
            return URI.create("f3synthetic://" + binding.getParent().getUri() + "/" + binding.getDefinition().getName());
        }
        LogicalCompositeComponent composite = binding.getParent().getParent().getParent();
        URI parent = composite.getUri();

        String bindingTarget = bindingTargetUri.toString();

        URI targetUri;
        if (bindingTarget.contains("/")) {
            String[] tokens = bindingTarget.split("/");
            if (tokens.length != 2) {
                throw new GenerationException("Invalid target specified on binding: " + bindingTarget);
            }
            targetUri = URI.create(parent.toString() + "/" + tokens[0]);
            LogicalComponent<?> component = composite.getComponent(targetUri);
            if (component == null) {
                throw new GenerationException("Target component not found: " + targetUri);
            }
            LogicalService service = component.getService(tokens[1]);
            if (service == null) {
                throw new GenerationException("Target service not found on component " + targetUri + ": " + tokens[1]);
            }
            // get the leaf service as the target may be a promotion
            targetUri = service.getLeafService().getUri();
        } else {
            targetUri = URI.create(parent.toString() + "/" + bindingTarget);
            if (targetUri.getFragment() == null) {
                LogicalComponent<?> component = composite.getComponent(targetUri);
                if (component == null) {
                    throw new GenerationException("Target component not found: " + targetUri);
                }
                if (component.getServices().size() != 1) {
                    throw new GenerationException("Target component must have exactly one service if the service is not specified in the target URI");
                }
                Collection<LogicalService> services = component.getServices();
                LogicalService service = services.iterator().next();
                // get the leaf service as the target may be a promotion
                targetUri = service.getLeafService().getUri();
            } else {
                URI defragmented = UriHelper.getDefragmentedName(targetUri);
                LogicalComponent component = composite.getComponent(defragmented);
                if (component == null) {
                    throw new GenerationException("Target component not found: " + targetUri);
                }

            }
        }
        return targetUri;
    }

    private void validateServiceContract(ServiceContract contract) throws InvalidContractException {
        boolean oneway = false;
        boolean first = true;
        for (Operation operation : contract.getOperations()) {
            if (first) {
                oneway = operation.getIntents().contains(ONEWAY);
                first = false;
            } else {
                boolean oneWayIntent = operation.getIntents().contains(ONEWAY);
                if ((!oneway && oneWayIntent) || (oneway && !oneWayIntent)) {
                    String name = contract.getInterfaceName();
                    throw new InvalidContractException("The ZeroMQ binding does not support mixing one-way and request-response operations: " + name);
                }
            }
        }
    }

}
