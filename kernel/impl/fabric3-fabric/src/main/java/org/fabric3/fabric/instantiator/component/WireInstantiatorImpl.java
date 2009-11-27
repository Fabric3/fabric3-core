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
package org.fabric3.fabric.instantiator.component;

import java.net.URI;

import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.WireDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the WireInstantiator.
 *
 * @version $Rev$ $Date$
 */
public class WireInstantiatorImpl implements WireInstantiator {

    public void instantiateWires(Composite composite, LogicalCompositeComponent parent, InstantiationContext context) {
        String baseUri = parent.getUri().toString();
        // instantiate wires held directly in the composite and in included composites
        for (WireDefinition definition : composite.getWires()) {
            // resolve the source reference
            // source URI is relative to the parent composite the include is targeted to
            URI sourceUri = URI.create(baseUri + "/" + UriHelper.getDefragmentedName(definition.getSource()));
            String referenceName = definition.getSource().getFragment();
            LogicalReference logicalReference = resolveLogicalReference(referenceName, sourceUri, parent, context);
            if (logicalReference == null) {
                // error resolving, continue
                continue;
            }

            // resolve the target service
            URI targetUri = URI.create(baseUri + "/" + definition.getTarget());
            targetUri = resolveTargetUri(targetUri, parent, context);
            if (targetUri == null) {
                // error resolving
                continue;
            }

            // create the wire
            LogicalWire wire = new LogicalWire(parent, logicalReference, targetUri);
            parent.addWire(logicalReference, wire);
        }
    }

    private LogicalReference resolveLogicalReference(String referenceName,
                                                     URI sourceUri,
                                                     LogicalCompositeComponent parent,
                                                     InstantiationContext context) {
        LogicalComponent<?> source = parent.getComponent(sourceUri);
        if (source == null) {
            URI uri = parent.getUri();
            URI contributionUri = parent.getDefinition().getContributionUri();
            WireSourceNotFound error = new WireSourceNotFound(sourceUri, uri, contributionUri);
            context.addError(error);
            return null;
        }
        LogicalReference logicalReference;
        if (referenceName == null) {
            // a reference was not specified
            if (source.getReferences().size() == 0) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireSourceNoReference error = new WireSourceNoReference(sourceUri, uri, contributionUri);
                context.addError(error);
                return null;
            } else if (source.getReferences().size() != 1) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireSourceAmbiguousReference error = new WireSourceAmbiguousReference(sourceUri, uri, contributionUri);
                context.addError(error);
                return null;
            }
            // default to the only reference
            logicalReference = source.getReferences().iterator().next();
        } else {
            logicalReference = source.getReference(referenceName);
            if (logicalReference == null) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireSourceReferenceNotFound error = new WireSourceReferenceNotFound(sourceUri, referenceName, uri, contributionUri);
                context.addError(error);
                return null;
            }
        }
        return logicalReference;
    }

    /**
     * Resolves the wire target URI to a service provided by a component in the parent composite.
     *
     * @param targetUri the atrget URI to resolve.
     * @param parent    the parent composite to resolve against
     * @param context   the logical context to report errors against
     * @return the fully resolved wire target URI
     */
    private URI resolveTargetUri(URI targetUri, LogicalCompositeComponent parent, InstantiationContext context) {
        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = parent.getComponent(targetComponentUri);
        if (targetComponent == null) {
            URI uri = parent.getUri();
            URI contributionUri = parent.getDefinition().getContributionUri();
            WireTargetNotFound error = new WireTargetNotFound(targetUri, uri, contributionUri);
            context.addError(error);
            return null;
        }

        String serviceName = targetUri.getFragment();
        if (serviceName != null) {
            if (targetComponent.getService(serviceName) == null) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireTargetServiceNotFound error = new WireTargetServiceNotFound(targetUri, uri, contributionUri);
                context.addError(error);
                return null;
            }
            return targetUri;
        } else {
            LogicalService target = null;
            for (LogicalService service : targetComponent.getServices()) {
                if (service.getDefinition().isManagement()) {
                    continue;
                }
                if (target != null) {
                    URI uri = parent.getUri();
                    URI contributionUri = parent.getDefinition().getContributionUri();
                    AmbiguousWireTargetService error = new AmbiguousWireTargetService(uri, targetUri, contributionUri);
                    context.addError(error);
                    return null;
                }
                target = service;
            }
            if (target == null) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireTargetNoService error = new WireTargetNoService(targetUri, uri, contributionUri);
                context.addError(error);
                return null;
            }
            return target.getUri();
        }

    }

}
