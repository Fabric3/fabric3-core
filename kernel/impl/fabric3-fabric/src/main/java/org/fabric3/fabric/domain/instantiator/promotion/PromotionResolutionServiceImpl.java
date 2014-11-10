/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.instantiator.promotion;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.fabric3.fabric.domain.instantiator.AmbiguousReference;
import org.fabric3.fabric.domain.instantiator.AmbiguousService;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.domain.instantiator.PromotedComponentNotFound;
import org.fabric3.fabric.domain.instantiator.PromotionResolutionService;
import org.fabric3.fabric.domain.instantiator.ReferenceNotFound;
import org.fabric3.fabric.domain.instantiator.ServiceNotFound;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the promotion service.
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
