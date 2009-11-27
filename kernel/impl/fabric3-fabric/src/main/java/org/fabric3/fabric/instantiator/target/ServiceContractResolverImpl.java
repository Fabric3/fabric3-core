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
package org.fabric3.fabric.instantiator.target;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;

/**
 * @version $Rev$ $Date$
 */
public class ServiceContractResolverImpl implements ServiceContractResolver {

    public ServiceContract determineContract(LogicalService service) {
        ServiceContract contract = service.getDefinition().getServiceContract();
        if (contract != null) {
            return contract;
        }
        if (!(service.getParent() instanceof LogicalCompositeComponent)) {
            return null;
        }
        LogicalCompositeComponent parent = (LogicalCompositeComponent) service.getParent();
        URI promotedUri = service.getPromotedUri();
        URI name = UriHelper.getDefragmentedName(promotedUri);
        LogicalComponent<?> promoted = parent.getComponent(name);
        if (promoted == null) {
            // This error should be caught by validation before this point
            throw new AssertionError("Promoted component " + name + " not found in " + parent.getUri());
        }
        String serviceName = promotedUri.getFragment();
        LogicalService promotedService;
        if (serviceName == null && promoted.getServices().size() == 1) {
            // select the default service as a service name was not specified
            Collection<LogicalService> services = promoted.getServices();
            promotedService = services.iterator().next();
        } else if (serviceName == null) {
            // This error should be caught by validation before this point
            throw new AssertionError("Service must be specified for promotion: " + promotedUri);
        } else {
            promotedService = promoted.getService(serviceName);
        }
        if (promotedService == null) {
            // This error should be caught by validation before this point
            throw new AssertionError("Promoted service not found: " + promotedUri);
        }
        return determineContract(promotedService);
    }

    public ServiceContract determineContract(LogicalReference reference) {
        ServiceContract contract = reference.getDefinition().getServiceContract();
        if (contract != null) {
            return contract;
        }
        if (!(reference.getParent() instanceof LogicalCompositeComponent)) {
            return null;
        }
        LogicalCompositeComponent parent = (LogicalCompositeComponent) reference.getParent();
        //URI promotedUri = service.getPromotedUri();
        List<URI> promotedUris = reference.getPromotedUris();
        if (promotedUris.size() < 1) {
            // this is an invalid configuration: a reference with no service contract that does not promote another reference and should be
            // caught during the load phase before reaching here.
            throw new AssertionError(" This is an Invalid Configuration on " + contract.getInterfaceName());
        }
        // pick the first one since references expose the same contract
        URI promotedUri = promotedUris.get(0);

        LogicalComponent<?> promoted = parent.getComponent(UriHelper.getDefragmentedName(promotedUri));
        assert promoted != null;
        String referenceName = promotedUri.getFragment();
        LogicalReference promotedReference;
        if (referenceName == null && promoted.getReferences().size() == 1) {
            // select the default reference as a reference name wast specified
            Collection<LogicalReference> references = promoted.getReferences();
            promotedReference = references.iterator().next();
        } else if (referenceName == null) {
            // programing error
            throw new AssertionError("Reference name must be specified on " + promoted.getDefinition().getName());
        } else {
            promotedReference = promoted.getReference(referenceName);
        }
        if (promotedReference == null) {
            throw new AssertionError("Promoted reference " + referenceName + " not found on " + promoted.getDefinition().getName());
        }
        return determineContract(promotedReference);
    }

}
