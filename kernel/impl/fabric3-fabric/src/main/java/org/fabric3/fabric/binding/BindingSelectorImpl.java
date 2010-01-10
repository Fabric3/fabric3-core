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
package org.fabric3.fabric.binding;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.binding.provider.BindingMatchResult;
import org.fabric3.spi.binding.provider.BindingProvider;
import org.fabric3.spi.binding.provider.BindingSelectionException;
import org.fabric3.spi.binding.provider.BindingSelectionStrategy;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * Selects a binding provider by delegating to a BindingSelectionStrategy configured for the domain. For each wire, if a remote service has an
 * explicit binding, its configuration will be used to construct the reference binding. If a service does not have an explicit binding, the wire is
 * said to using binding.sca, in which case the BindingSelector will select an appropriate remote transport and create binding configuraton for both
 * sides of the wire.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class BindingSelectorImpl implements BindingSelector {
    private List<BindingProvider> providers = new ArrayList<BindingProvider>();
    private BindingSelectionStrategy strategy;

    /**
     * Lazily injects SCAServiceProviders as they become available from runtime extensions.
     *
     * @param providers the set of providers
     */
    @Reference(required = false)
    public void setProviders(List<BindingProvider> providers) {
        this.providers = providers;
        orderProviders();
    }

    @Reference(required = false)
    public void setStrategy(BindingSelectionStrategy strategy) {
        this.strategy = strategy;
    }

    @Init
    public void orderProviders() {
        if (strategy != null) {
            strategy.order(providers);
        }
    }


    public void selectBindings(LogicalComponent<?> component) throws BindingSelectionException {
        for (LogicalReference reference : component.getReferences()) {
            for (LogicalWire wire : reference.getWires()) {
                LogicalService targetService = wire.getTarget();
                if (targetService != null) {
                    LogicalComponent<?> targetComponent = targetService.getParent();
                    if ((component.getZone() == null && targetComponent.getZone() == null)) {
                        // components are local, no need for a binding
                        continue;
                    } else if (component.getZone() != null && component.getZone().equals(targetComponent.getZone())) {
                        // components are local, no need for a binding
                        continue;
                    }
                    selectBinding(wire);
                }
            }
        }
    }

    /**
     * Selects and configures a binding to connect the source to the target.
     *
     * @param wire the wire
     * @throws BindingSelectionException if an error occurs selecting a binding
     */
    private void selectBinding(LogicalWire wire) throws BindingSelectionException {
        List<BindingMatchResult> results = new ArrayList<BindingMatchResult>();
        LogicalReference source = wire.getSource();
        LogicalService target = wire.getTarget();
        for (BindingProvider provider : providers) {
            BindingMatchResult result = provider.canBind(source, target);
            if (result.isMatch()) {
                provider.bind(source, target);
                wire.setSourceBinding(source.getBindings().get(0));
                wire.setTargetBinding(target.getBindings().get(0));
                return;
            }
            results.add(result);

        }
        URI sourceUri = source.getUri();
        URI targetUri = target.getUri();
        throw new NoSCABindingProviderException("No SCA binding provider suitable for creating wire from " + sourceUri + " to " + targetUri, results);
    }

}

