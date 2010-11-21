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
*/
package org.fabric3.binding.net.generator;

import java.net.URI;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.net.model.HttpBindingDefinition;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.binding.provider.BindingMatchResult;
import org.fabric3.spi.binding.provider.BindingProvider;
import org.fabric3.spi.binding.provider.BindingSelectionException;
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * Creates logical configuration for binding.sca using the HTTP binding.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class HttpBindingProvider implements BindingProvider {
    private static final QName HTTP_BINDING = new QName(Constants.SCA_NS, "binding.http");
    private static final BindingMatchResult NO_MATCH = new BindingMatchResult(false, HTTP_BINDING);

    private DomainTopologyService topologyService;
    private boolean enabled = true;

    /**
     * Injects the domain manager. This reference is optional so the extension can be loaded on non-controller instances.
     *
     * @param topologyService the domain manager
     */
    @Reference(required = false)
    public void setTopologyService(DomainTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Property(required = false)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public QName getType() {
        return HTTP_BINDING;
    }

    public BindingMatchResult canBind(LogicalWire wire) {
        if (!enabled) {
            return NO_MATCH;
        }
        LogicalReference source = wire.getSource().getLeafReference();
        ServiceContract contract = source.getDefinition().getServiceContract();
        for (Operation operation : contract.getOperations()) {
            if (operation.getInputTypes().size() > 1) {
                BindingMatchResult result = new BindingMatchResult(false, HTTP_BINDING);
                result.addReason("Operations with more than one parameter not supported");
                return result;
            }
        }
        ServiceContract callbackContract = contract.getCallbackContract();
        if (callbackContract != null) {
            for (Operation operation : callbackContract.getOperations()) {
                if (operation.getInputTypes().size() > 1) {
                    BindingMatchResult result = new BindingMatchResult(false, HTTP_BINDING);
                    result.addReason("Operations with more than one parameter not supported");
                    return result;
                }
            }
        }
        return new BindingMatchResult(true, HTTP_BINDING);
    }

    public BindingMatchResult canBind(LogicalChannel channel) {
        // does not support eventing
        return NO_MATCH;
    }

    public void bind(LogicalWire wire) throws BindingSelectionException {
        if (topologyService == null) {
            throw new BindingSelectionException("Domain manager not configured");
        }
        LogicalReference source = wire.getSource().getLeafReference();
        LogicalService target = wire.getTarget().getLeafService();
        LogicalComponent<?> targetComponent = target.getParent();
        String targetZone = targetComponent.getZone();

        // get the base URL for the target cluster
        String targetBaseUrl = topologyService.getTransportMetaData(targetZone, "binding.net.http");
        if (targetBaseUrl == null) {
            throw new BindingSelectionException("Target HTTP information not found");
        }
        targetBaseUrl = "http://" + targetBaseUrl;
        // determing whether to configure both sides of the wire or just the reference
        if (target.getBindings().isEmpty()) {
            // configure both sides
            configureService(source, target);
            configureReference(source, target, targetBaseUrl);
        } else {
            configureReference(source, target, targetBaseUrl);
        }
        if (target.getDefinition().getServiceContract().getCallbackContract() != null) {
            configureCallback(source, target);

        }
    }

    public void bind(LogicalChannel channel) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    private void configureReference(LogicalReference source, LogicalService target, String baseUrl) throws BindingSelectionException {
        LogicalBinding<HttpBindingDefinition> binding = null;
        for (LogicalBinding<?> entry : target.getBindings()) {
            if (entry.getDefinition().getType().equals(HTTP_BINDING)) {
                binding = (LogicalBinding<HttpBindingDefinition>) entry;
                break;
            }
        }
        if (binding == null) {
            throw new BindingSelectionException("HTTP binding on service not found: " + target.getUri());
        }
        URI targetUri = URI.create(baseUrl + binding.getDefinition().getTargetUri().toString());
        constructLogicalReference(source, targetUri);
    }

    private void constructLogicalReference(LogicalReference source, URI targetUri) {
        HttpBindingDefinition referenceDefinition = new HttpBindingDefinition(targetUri);
        LogicalBinding<HttpBindingDefinition> referenceBinding = new LogicalBinding<HttpBindingDefinition>(referenceDefinition, source);
        referenceBinding.setAssigned(true);
        source.addBinding(referenceBinding);
    }

    private void configureService(LogicalReference source, LogicalService target) {
        String endpointName = target.getUri().getPath() + "/" + target.getUri().getFragment();
        URI endpointUri = URI.create(endpointName);
        HttpBindingDefinition serviceDefinition = new HttpBindingDefinition(endpointUri);
        QName deployable = source.getParent().getDeployable();
        LogicalBinding<HttpBindingDefinition> serviceBinding = new LogicalBinding<HttpBindingDefinition>(serviceDefinition, target, deployable);
        serviceBinding.setAssigned(true);
        target.addBinding(serviceBinding);
    }

    private void configureCallback(LogicalReference source, LogicalService target) throws BindingSelectionException {
        LogicalComponent<?> sourceComponent = source.getParent();
        String sourceZone = sourceComponent.getZone();

        // get the base URL for the source cluster
        String sourceBaseUrl = topologyService.getTransportMetaData(sourceZone, "binding.net.http");
        if (sourceBaseUrl == null) {
            throw new BindingSelectionException("Source HTTP information not found");
        }
        sourceBaseUrl = "http://" + sourceBaseUrl;
        // configure the callback service on the source side
        String endpointName = target.getUri().getPath() + "/" + source.getUri().getFragment();
        URI endpointUri = URI.create(endpointName);

        HttpBindingDefinition sourceCallbackDefinition = new HttpBindingDefinition(endpointUri);
        LogicalBinding<HttpBindingDefinition> sourceBinding = new LogicalBinding<HttpBindingDefinition>(sourceCallbackDefinition, source);
        sourceBinding.setAssigned(true);
        source.addCallbackBinding(sourceBinding);

        URI callbackUri = URI.create(sourceBaseUrl + endpointName);
        HttpBindingDefinition targetCallbackDefinition = new HttpBindingDefinition(callbackUri);
        LogicalBinding<HttpBindingDefinition> targetBinding = new LogicalBinding<HttpBindingDefinition>(targetCallbackDefinition, target);
        targetBinding.setAssigned(true);
        target.addCallbackBinding(targetBinding);
    }

}
