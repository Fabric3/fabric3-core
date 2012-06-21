/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
import java.util.List;

import org.fabric3.fabric.instantiator.AmbiguousReference;
import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.instantiator.PromotedComponentNotFound;
import org.fabric3.fabric.instantiator.PromotionResolutionService;
import org.fabric3.fabric.instantiator.ReferenceNotFound;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the promotion service.
 *
 * @version $Rev$ $Date$
 */
public class PromotionResolutionServiceImpl implements PromotionResolutionService {

    public void resolve(LogicalComponent<?> component, InstantiationContext context) {
        resolveReferences(component, context);
        resolveServices(component, context);
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                resolve(child, context);
            }
        }
    }

    private void resolveReferences(LogicalComponent<?> component, InstantiationContext context) {
        for (LogicalReference reference : component.getReferences()) {
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

    private void resolveServices(LogicalComponent<?> component, InstantiationContext context) {
        for (LogicalService logicalService : component.getServices()) {
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
            PromotedComponentNotFound error = new PromotedComponentNotFound(logicalService, promotedComponentUri);
            context.addError(error);
            return;
        }

        if (promotedServiceName == null) {
            Collection<LogicalService> componentServices = promotedComponent.getServices();
            if (componentServices.size() == 0) {
                raiseNoServiceError(logicalService, promotedComponentUri, context);
            } else if (componentServices.size() == 2) {
                raiseAmbiguousServiceError(logicalService, promotedComponentUri, context);
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

    void resolve(LogicalReference reference, InstantiationContext context) {

        List<URI> promotedUris = reference.getPromotedUris();

        for (int i = 0; i < promotedUris.size(); i++) {

            URI promotedUri = promotedUris.get(i);

            URI promotedComponentUri = UriHelper.getDefragmentedName(promotedUri);
            String promotedReferenceName = promotedUri.getFragment();

            LogicalCompositeComponent parent = (LogicalCompositeComponent) reference.getParent();
            LogicalComponent<?> promotedComponent = parent.getComponent(promotedComponentUri);

            if (promotedComponent == null) {
                raiseComponentNotFoundError(reference, promotedComponentUri, context);
                return;
            }

            if (promotedReferenceName == null) {
                Collection<LogicalReference> componentReferences = promotedComponent.getReferences();
                if (componentReferences.size() == 0) {
                    raiseReferenceNotFoundError(promotedReferenceName, promotedComponentUri, reference, context);
                    return;
                } else if (componentReferences.size() > 1) {
                    AmbiguousReference error = new AmbiguousReference(reference, promotedComponentUri);
                    context.addError(error);
                    return;
                }
                LogicalReference promotedReference = componentReferences.iterator().next();
                reference.setPromotedUri(i, promotedReference.getUri());
                // mark the promoted reference as resolved but not the current reference being evaluated since it may by at the top of the promotion
                // hierarchy and need to be resolved
                promotedReference.setResolved(true);
            } else {
                LogicalReference promotedReference = promotedComponent.getReference(promotedReferenceName);
                if (promotedReference == null) {
                    raiseReferenceNotFoundError(promotedReferenceName, promotedComponentUri, reference, context);
                    return;
                }
                // mark the promoted reference as resolved but not the current reference being evaluated since it may by at the top of the promotion
                // hierarchy and need to be resolved
                promotedReference.setResolved(true);
            }

        }

    }

    private void raiseReferenceNotFoundError(String referenceName, URI uri, LogicalReference reference, InstantiationContext context) {
        String msg = "Reference " + referenceName + " not found on component " + uri;
        ReferenceNotFound error = new ReferenceNotFound(msg, reference);
        context.addError(error);
    }

    private void raiseComponentNotFoundError(LogicalReference reference, URI uri, InstantiationContext context) {
        PromotedComponentNotFound error = new PromotedComponentNotFound(reference, uri);
        context.addError(error);
    }

    private void raiseServiceNotFoundError(LogicalService service, URI uri, String name, InstantiationContext context) {
        String message = "Service " + name + " promoted from " + service.getUri() + " not found on component " + uri;
        ServiceNotFound error = new ServiceNotFound(message, service);
        context.addError(error);
    }

    private void raiseNoServiceError(LogicalService service, URI uri, InstantiationContext context) {
        LogicalComponent<?> parent = service.getParent();
        String msg = "No services available on component: " + uri;
        NoServiceOnComponent error = new NoServiceOnComponent(msg, parent);
        context.addError(error);
    }

    private void raiseAmbiguousServiceError(LogicalService service, URI uri, InstantiationContext context) {
        String msg = "The promoted service " + service.getUri() + " must explicitly specify the service it is promoting on component "
                + uri + " as the component has more than one service";
        AmbiguousService error = new AmbiguousService(msg, service);
        context.addError(error);
    }


}
