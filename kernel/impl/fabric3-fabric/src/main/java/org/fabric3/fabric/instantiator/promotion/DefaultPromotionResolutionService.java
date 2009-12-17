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
package org.fabric3.fabric.instantiator.promotion;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.fabric3.fabric.instantiator.AmbiguousReference;
import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.instantiator.PromotedComponentNotFound;
import org.fabric3.fabric.instantiator.PromotionResolutionService;
import org.fabric3.fabric.instantiator.ReferenceNotFound;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.model.type.component.Multiplicity;

/**
 * Default implementation of the promotion service.
 *
 * @version $Rev$ $Date$
 */
public class DefaultPromotionResolutionService implements PromotionResolutionService {

    public void resolve(LogicalComponent<?> logicalComponent, InstantiationContext context) {
        resolveReferences(logicalComponent, context);
        resolveServices(logicalComponent, context);
        if (logicalComponent instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) logicalComponent;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                resolve(child, context);
            }
        }
    }

    private void resolveReferences(LogicalComponent<?> logicalComponent, InstantiationContext context) {
        for (LogicalReference reference : logicalComponent.getReferences()) {
            Multiplicity multiplicityValue = reference.getDefinition().getMultiplicity();
            boolean refMultiplicity = multiplicityValue.equals(Multiplicity.ZERO_N) || multiplicityValue.equals(Multiplicity.ONE_N);
            if (refMultiplicity || !reference.isResolved()) {
                // Only resolve references that have not been resolved or ones that are multiplicities since the latter may be reinjected.
                // Explicitly set the reference to unresolved, since if it was a multiplicity it may have been previously resolved.
                reference.setResolved(false);
                resolve(reference, context);
            }
        }
    }

    private void resolveServices(LogicalComponent<?> logicalComponent, InstantiationContext context) {
        for (LogicalService logicalService : logicalComponent.getServices()) {
            resolve(logicalService, context);
        }
    }


    void resolve(LogicalService logicalService, InstantiationContext context) {

        URI promotedUri = logicalService.getPromotedUri();

        if (promotedUri == null) {
            return;
        }

        URI promotedComponentUri = UriHelper.getDefragmentedName(promotedUri);
        String promotedServiceName = promotedUri.getFragment();

        LogicalCompositeComponent composite = (LogicalCompositeComponent) logicalService.getParent();
        LogicalComponent<?> promotedComponent = composite.getComponent(promotedComponentUri);

        if (promotedComponent == null) {
            LogicalComponent<?> parent = logicalService.getParent();
            URI componentUri = parent.getUri();
            URI serviceUri = logicalService.getUri();
            URI contributionUri = parent.getDefinition().getContributionUri();
            PromotedComponentNotFound error = new PromotedComponentNotFound(serviceUri, promotedComponentUri, componentUri, contributionUri);
            context.addError(error);
            return;
        }

        if (promotedServiceName == null) {
            Collection<LogicalService> componentServices = promotedComponent.getServices();
            if (componentServices.size() == 0) {
                raiseNoServiceError(logicalService, promotedComponentUri, context);
            } else if (componentServices.size() == 2) {
                Iterator<LogicalService> iter = componentServices.iterator();
                LogicalService one = iter.next();
                LogicalService two = iter.next();
                if (one.getDefinition().isManagement()) {
                    logicalService.setPromotedUri(two.getUri());
                } else if (two.getDefinition().isManagement()) {
                    logicalService.setPromotedUri(one.getUri());
                } else {
                    raiseAmbiguousServiceError(logicalService, promotedComponentUri, context);
                }
            } else if (componentServices.size() > 2) {
                raiseAmbiguousServiceError(logicalService, promotedComponentUri, context);
            } else {
                logicalService.setPromotedUri(componentServices.iterator().next().getUri());
            }
        } else {
            if (promotedComponent.getService(promotedServiceName) == null) {
                raiseServiceNotFoundError(logicalService, promotedComponentUri, promotedServiceName, context);
            }
        }

    }

    void resolve(LogicalReference logicalReference, InstantiationContext context) {

        List<URI> promotedUris = logicalReference.getPromotedUris();

        for (int i = 0; i < promotedUris.size(); i++) {

            URI promotedUri = promotedUris.get(i);

            URI promotedComponentUri = UriHelper.getDefragmentedName(promotedUri);
            String promotedReferenceName = promotedUri.getFragment();

            LogicalCompositeComponent parent = (LogicalCompositeComponent) logicalReference.getParent();
            LogicalComponent<?> promotedComponent = parent.getComponent(promotedComponentUri);

            if (promotedComponent == null) {
                URI componentUri = parent.getUri();
                URI referenceUri = logicalReference.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                PromotedComponentNotFound error = new PromotedComponentNotFound(referenceUri, promotedComponentUri, componentUri, contributionUri);
                context.addError(error);
                return;
            }

            if (promotedReferenceName == null) {
                Collection<LogicalReference> componentReferences = promotedComponent.getReferences();
                if (componentReferences.size() == 0) {
                    String msg = "Reference " + promotedReferenceName + " not found on component " + promotedComponentUri;
                    URI componentUri = parent.getUri();
                    URI contributionUri = parent.getDefinition().getContributionUri();
                    ReferenceNotFound error = new ReferenceNotFound(msg, promotedReferenceName, componentUri, contributionUri);
                    context.addError(error);
                    return;
                } else if (componentReferences.size() > 1) {
                    URI referenceUri = logicalReference.getUri();
                    URI contributionUri = parent.getDefinition().getContributionUri();
                    AmbiguousReference error = new AmbiguousReference(referenceUri, parent.getUri(), promotedComponentUri, contributionUri);
                    context.addError(error);
                    return;
                }
                LogicalReference promotedReference = componentReferences.iterator().next();
                logicalReference.setPromotedUri(i, promotedReference.getUri());
                // mark the promoted reference as resolved but not the current reference being evaluated since it may by at the top of the promotion
                // hierarchy and need to be resolved
                promotedReference.setResolved(true);
            } else {
                LogicalReference promotedReference = promotedComponent.getReference(promotedReferenceName);
                if (promotedReference == null) {

                    String msg = "Reference " + promotedReferenceName + " not found on component " + promotedComponentUri;
                    URI componentUri = parent.getUri();
                    URI contributionUri = parent.getDefinition().getContributionUri();
                    ReferenceNotFound error = new ReferenceNotFound(msg, promotedReferenceName, componentUri, contributionUri);
                    context.addError(error);
                    return;
                }
                // mark the promoted reference as resolved but not the current reference being evaluated since it may by at the top of the promotion
                // hierarchy and need to be resolved
                promotedReference.setResolved(true);
            }

        }

    }

    private void raiseServiceNotFoundError(LogicalService logicalService, URI uri, String name, InstantiationContext context) {
        String message = "Service " + name + " promoted from " + logicalService.getUri() + " not found on component " + uri;
        URI componentUri = logicalService.getParent().getUri();
        URI contributionUri = logicalService.getParent().getDefinition().getContributionUri();
        URI serviceUri = logicalService.getUri();
        ServiceNotFound error = new ServiceNotFound(message, serviceUri, componentUri, contributionUri);
        context.addError(error);
    }

    private void raiseNoServiceError(LogicalService logicalService, URI uri, InstantiationContext context) {
        LogicalComponent<?> parent = logicalService.getParent();
        URI componentUri = parent.getUri();
        URI contributionUri = parent.getDefinition().getContributionUri();
        String msg = "No services available on component: " + uri;
        NoServiceOnComponent error = new NoServiceOnComponent(msg, componentUri, contributionUri);
        context.addError(error);
    }

    private void raiseAmbiguousServiceError(LogicalService logicalService, URI uri, InstantiationContext context) {
        String msg = "The promoted service " + logicalService.getUri() + " must explicitly specify the service it is promoting on component "
                + uri + " as the component has more than one service";
        LogicalComponent<?> parent = logicalService.getParent();
        URI componentUri = parent.getUri();
        URI contributionUri = parent.getDefinition().getContributionUri();
        AmbiguousService error = new AmbiguousService(msg, componentUri, contributionUri);
        context.addError(error);
    }


}
