/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
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

import java.net.URI;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.binding.zeromq.provision.ZeroMQSourceDefinition;
import org.fabric3.binding.zeromq.provision.ZeroMQTargetDefinition;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.util.UriHelper;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class ZeroMQBindingGenerator implements BindingGenerator<ZeroMQBindingDefinition> {
    private static final QName IMMEDIATE_ONEWAY = new QName(Constants.SCA_NS, "immediateOneWay");
    private static final QName ONEWAY = new QName(Constants.SCA_NS, "oneWay");

    public ZeroMQSourceDefinition generateSource(LogicalBinding<ZeroMQBindingDefinition> binding,
                                                 ServiceContract contract,
                                                 List<LogicalOperation> operations,
                                                 EffectivePolicy policy) throws GenerationException {
        if (binding.isCallback()) {
            URI uri = URI.create("zmq://" + contract.getInterfaceName());
            return new ZeroMQSourceDefinition(uri);
        } else {
            return new ZeroMQSourceDefinition();
        }
    }

    public ZeroMQTargetDefinition generateTarget(LogicalBinding<ZeroMQBindingDefinition> binding,
                                                 ServiceContract contract,
                                                 List<LogicalOperation> operations,
                                                 EffectivePolicy policy) throws GenerationException {
        validateServiceContract(contract);

        if (binding.isCallback()) {
            URI targetUri = URI.create("zmq://" + contract.getInterfaceName());
            return new ZeroMQTargetDefinition(targetUri);
        }
        LogicalCompositeComponent composite = binding.getParent().getParent().getParent();
        URI parent = composite.getUri();
        URI targetUri = URI.create(parent.toString() + "/" + binding.getDefinition().getTargetUri());
        if (targetUri.getFragment() == null) {
            LogicalComponent<?> component = composite.getComponent(targetUri);
            if (component == null) {
                throw new GenerationException("Target component not found: " + targetUri);
            }
            if (component.getServices().size() != 1) {
                throw new GenerationException("Target component must have exactly one service if the service is not specified in the target URI");
            }
            Collection<LogicalService> services = component.getServices();
            targetUri = services.iterator().next().getUri();
        } else {
            URI defragmented = UriHelper.getDefragmentedName(targetUri);
            LogicalComponent component = composite.getComponent(defragmented);
            if (component == null) {
                throw new GenerationException("Target component not found: " + targetUri);
            }

        }
        boolean hasCallback = contract.getCallbackContract() != null;
        if (hasCallback) {
            URI callbackUri = URI.create("zmq://" + contract.getCallbackContract().getInterfaceName());
            return new ZeroMQTargetDefinition(targetUri, callbackUri);
        }
        return new ZeroMQTargetDefinition(targetUri);
    }

    public PhysicalTargetDefinition generateServiceBindingTarget(LogicalBinding<ZeroMQBindingDefinition> binding,
                                                                 ServiceContract contract,
                                                                 List<LogicalOperation> operations,
                                                                 EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    private void validateServiceContract(ServiceContract contract) throws InvalidContractException {
        boolean oneway = false;
        boolean first = true;
        for (Operation operation : contract.getOperations()) {
            if (first) {
                oneway = operation.getIntents().contains(ONEWAY);
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
