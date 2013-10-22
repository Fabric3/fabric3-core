/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.fabric.deployment.instantiator.promotion;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.fabric.deployment.instantiator.AmbiguousReference;
import org.fabric3.fabric.deployment.instantiator.AmbiguousService;
import org.fabric3.fabric.deployment.instantiator.InstantiationContext;
import org.fabric3.fabric.deployment.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.deployment.instantiator.PromotedComponentNotFound;
import org.fabric3.fabric.deployment.instantiator.ReferenceNotFound;
import org.fabric3.fabric.deployment.instantiator.ServiceNotFound;
import org.fabric3.spi.model.type.system.SystemImplementation;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

public class PromotionServiceImplTestCase extends TestCase {

    private PromotionResolutionServiceImpl promotionResolutionService;
    private LogicalCompositeComponent domain;

    public void testNoComponentForPromotedService() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service"));

        InstantiationContext change = new InstantiationContext();
        promotionResolutionService.resolve(logicalService, change);
        assertTrue(change.getErrors().get(0) instanceof PromotedComponentNotFound);
    }

    public void testMultipleServicesWithNoServiceFragment() throws Exception {
        ServiceDefinition definition = new ServiceDefinition("");
        LogicalService logicalService = new LogicalService(URI.create("service"), definition, domain);
        logicalService.setPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), definition, domain));
        logicalComponent.addService(new LogicalService(URI.create("component#service2"), definition, domain));

        domain.addComponent(logicalComponent);
        InstantiationContext change = new InstantiationContext();
        promotionResolutionService.resolve(logicalService, change);
        assertTrue(change.getErrors().get(0) instanceof AmbiguousService);
    }

    public void testNoServiceWithNoServiceFragment() throws Exception {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);

        domain.addComponent(logicalComponent);
        InstantiationContext change = new InstantiationContext();
        promotionResolutionService.resolve(logicalService, change);
        assertTrue(change.getErrors().get(0) instanceof NoServiceOnComponent);
    }

    public void testNoServiceWithServiceFragment() throws Exception {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);

        domain.addComponent(logicalComponent);

        InstantiationContext change = new InstantiationContext();
        promotionResolutionService.resolve(logicalService, change);
        assertTrue(change.getErrors().get(0) instanceof ServiceNotFound);
    }

    public void testNoServiceFragment() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), null, domain));
        domain.addComponent(logicalComponent);

        InstantiationContext change = new InstantiationContext();
        promotionResolutionService.resolve(logicalService, change);
        assertEquals(URI.create("component#service1"), logicalService.getPromotedUri());

    }

    public void testWithServiceFragment() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service1"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), null, domain));
        domain.addComponent(logicalComponent);
        InstantiationContext change = new InstantiationContext();
        promotionResolutionService.resolve(logicalService, change);
    }

    public void testNoComponentForPromotedReference() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#service"));

        InstantiationContext change = new InstantiationContext();
        promotionResolutionService.resolve(logicalReference, change);
        assertTrue(change.getErrors().get(0) instanceof PromotedComponentNotFound);

    }

    public void testMultipleReferencesWithNoReferenceFragment() throws Exception {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference2"), null, domain));

        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext();
        promotionResolutionService.resolve(logicalReference, context);
        assertTrue(context.getErrors().get(0) instanceof AmbiguousReference);

    }

    public void testNoReferenceWithNoReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);


        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext();
        promotionResolutionService.resolve(logicalReference, context);
        assert (context.getErrors().get(0) instanceof ReferenceNotFound);
    }

    public void testNoReferenceWithReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#reference"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);


        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext();
        promotionResolutionService.resolve(logicalReference, context);
        assertTrue(context.getErrors().get(0) instanceof ReferenceNotFound);
    }

    public void testNoReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext();
        promotionResolutionService.resolve(logicalReference, context);
        assertEquals(URI.create("component#reference1"), logicalReference.getPromotedUris().iterator().next());

    }

    public void testWithReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#reference1"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext();
        promotionResolutionService.resolve(logicalReference, context);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        promotionResolutionService = new PromotionResolutionServiceImpl();
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>("domain");
        URI uri = URI.create("fabric3://runtime");
        domain = new LogicalCompositeComponent(uri, definition, null);
    }
}
