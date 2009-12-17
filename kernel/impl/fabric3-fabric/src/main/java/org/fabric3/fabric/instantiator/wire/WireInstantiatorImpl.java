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
package org.fabric3.fabric.instantiator.wire;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.WireDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
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
    private ServiceContractResolver resolver;
    private ContractMatcher matcher;

    public WireInstantiatorImpl(@Reference ServiceContractResolver resolver, @Reference ContractMatcher matcher) {
        this.resolver = resolver;
        this.matcher = matcher;
    }

    public void instantiateCompositeWires(Composite composite, LogicalCompositeComponent parent, InstantiationContext context) {
        String baseUri = parent.getUri().toString();
        // instantiate wires held directly in the composite and in included composites
        for (WireDefinition definition : composite.getWires()) {
            // resolve the source reference
            // source URI is relative to the parent composite the include is targeted to
            URI sourceUri = URI.create(baseUri + "/" + UriHelper.getDefragmentedName(definition.getSource()));
            String referenceName = definition.getSource().getFragment();
            LogicalReference logicalReference = resolveLogicalReference(referenceName, sourceUri, parent, context);
            if (logicalReference == null) {
                // error resolving, continue processing other targets so all errors are collated
                continue;
            }

            // resolve the target service
            URI targetUri = URI.create(baseUri + "/" + definition.getTarget());
            LogicalService targetService = resolveTargetUri(targetUri, parent, context);
            if (targetUri == null) {
                // error resolving, continue processing other targets so all errors are collated
                continue;
            }

            // create the wire
            QName deployable = parent.getDeployable();
            LogicalWire wire = new LogicalWire(parent, logicalReference, targetService, deployable);
            parent.addWire(logicalReference, wire);
        }
    }


    public void instantiateReferenceWires(LogicalComponent<?> component, LogicalCompositeComponent parent, InstantiationContext context) {
        for (LogicalReference logicalReference : component.getReferences()) {
            resolve(logicalReference, parent, context);
        }
    }

    private void resolve(LogicalReference logicalReference, LogicalCompositeComponent component, InstantiationContext context) {

        ComponentReference componentReference = logicalReference.getComponentReference();
        if (componentReference == null) {
            // the reference is not configured on the component definition in the composite so there are no wires
            return;
        }

        List<URI> requestedTargets = componentReference.getTargets();
        if (requestedTargets.isEmpty()) {
            // no target urls are specified
            return;
        }

        URI parentUri = component.getUri();
        URI componentUri = logicalReference.getParent().getUri();

        // resolve the target URIs to services
        List<LogicalService> targets = new ArrayList<LogicalService>();
        for (URI requestedTarget : requestedTargets) {
            URI resolved = parentUri.resolve(componentUri).resolve(requestedTarget);
            LogicalService targetService = resolveByUri(logicalReference, resolved, component, context);
            if (targetService == null) {
                return;
            }
            targets.add(targetService);

        }
        // create the logical wires
        LogicalComponent parent = logicalReference.getParent();
        LogicalCompositeComponent grandParent = (LogicalCompositeComponent) parent.getParent();
        List<LogicalWire> wires = new ArrayList<LogicalWire>();
        if (null != grandParent) {
            for (LogicalService targetService : targets) {
                QName deployable = targetService.getParent().getDeployable();
                LogicalWire wire = new LogicalWire(grandParent, logicalReference, targetService, deployable);
                wires.add(wire);
            }
            grandParent.overrideWires(logicalReference, wires);
        } else {
            for (LogicalService targetService : targets) {
                QName deployable = targetService.getParent().getDeployable();
                LogicalWire wire = new LogicalWire(parent, logicalReference, targetService, deployable);
                wires.add(wire);
            }
            ((LogicalCompositeComponent) parent).overrideWires(logicalReference, wires);
        }
        logicalReference.setResolved(true);
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
    private LogicalService resolveTargetUri(URI targetUri, LogicalCompositeComponent parent, InstantiationContext context) {
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
            LogicalService targetService = targetComponent.getService(serviceName);
            if (targetService == null) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireTargetServiceNotFound error = new WireTargetServiceNotFound(targetUri, uri, contributionUri);
                context.addError(error);
                return null;
            }
            return targetService;
        } else {
            LogicalService target = null;
            for (LogicalService service : targetComponent.getServices()) {
                if (service.getDefinition().isManagement()) {
                    continue;
                }
                if (target != null) {
                    URI uri = parent.getUri();
                    URI contributionUri = parent.getDefinition().getContributionUri();
                    AmbiguousWireTarget error = new AmbiguousWireTarget(uri, targetUri, contributionUri);
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
            return target;
        }

    }


    private LogicalService resolveByUri(LogicalReference reference,
                                        URI targetUri,
                                        LogicalCompositeComponent composite,
                                        InstantiationContext context) {

        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = composite.getComponent(targetComponentUri);
        if (targetComponent == null) {
            URI referenceUri = reference.getUri();
            URI componentUri = reference.getParent().getUri();
            URI contributionUri = reference.getParent().getDefinition().getContributionUri();
            TargetComponentNotFound error = new TargetComponentNotFound(referenceUri, targetComponentUri, componentUri, contributionUri);
            context.addError(error);
            return null;
        }

        String serviceName = targetUri.getFragment();
        LogicalService targetService = null;
        if (serviceName != null) {
            targetService = targetComponent.getService(serviceName);
            if (targetService == null) {
                URI name = UriHelper.getDefragmentedName(targetUri);
                URI uri = reference.getUri();
                String msg = "The service " + serviceName + " targeted from the reference " + uri + " is not found on component " + name;
                URI componentUri = reference.getParent().getUri();
                ServiceNotFound error = new ServiceNotFound(msg, uri, componentUri, targetComponentUri);
                context.addError(error);
                return null;
            }
        } else {
            for (LogicalService service : targetComponent.getServices()) {
                if (service.getDefinition().isManagement()) {
                    continue;
                }
                if (targetService != null) {
                    String msg = "More than one service available on component: " + targetUri
                            + ". Reference must explicitly specify a target service: " + reference.getUri();
                    LogicalComponent<?> parent = reference.getParent();
                    URI componentUri = parent.getUri();
                    URI contributionUri = parent.getDefinition().getContributionUri();
                    AmbiguousService error = new AmbiguousService(msg, componentUri, contributionUri);
                    context.addError(error);
                    return null;
                }
                targetService = service;
            }
            if (targetService == null) {
                String msg = "The reference " + reference.getUri() + " is wired to component " + targetUri + " but the component has no services";
                LogicalComponent<?> parent = reference.getParent();
                URI componentUri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                NoServiceOnComponent error = new NoServiceOnComponent(msg, componentUri, contributionUri);
                context.addError(error);
                return null;
            }
        }
        validate(reference, targetService, context);
        return targetService;
    }

    private void validate(LogicalReference reference, LogicalService service, InstantiationContext context) {
        validateKeyedReference(reference, service, context);
        validateContracts(reference, service, context);
    }

    /**
     * Validates a target key is present for keyed references.
     *
     * @param reference the reference
     * @param service   the service
     * @param context   the logical context
     */
    private void validateKeyedReference(LogicalReference reference, LogicalService service, InstantiationContext context) {
        if (!reference.getDefinition().isKeyed()) {
            return;
        }
        LogicalComponent<?> parent = service.getParent();
        if (parent.getDefinition().getKey() == null) {
            KeyNotFound error = new KeyNotFound(reference.getUri(), parent.getUri(), parent.getDefinition().getContributionUri());
            context.addError(error);
        }
    }

    /**
     * Validates the reference and service contracts match.
     *
     * @param reference the reference
     * @param service   the service
     * @param context   the logical context
     */
    private void validateContracts(LogicalReference reference, LogicalService service, InstantiationContext context) {
        ServiceContract referenceContract = resolver.determineContract(reference);
        ServiceContract serviceContract = resolver.determineContract(service);
        MatchResult result = matcher.isAssignableFrom(referenceContract, serviceContract, true);
        if (!result.isAssignable()) {
            URI uri = reference.getParent().getUri();
            URI referenceUri = reference.getUri();
            URI serviceUri = service.getUri();
            URI contributionUri = reference.getParent().getDefinition().getContributionUri();
            String message = result.getError();
            IncompatibleContracts error = new IncompatibleContracts(referenceUri, serviceUri, uri, message, contributionUri);
            context.addError(error);
        }
    }

}
