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

import junit.framework.TestCase;

import org.fabric3.fabric.domain.instantiator.AmbiguousReference;
import org.fabric3.fabric.domain.instantiator.AmbiguousService;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.domain.instantiator.PromotedComponentNotFound;
import org.fabric3.fabric.domain.instantiator.ReferenceNotFound;
import org.fabric3.fabric.domain.instantiator.ServiceNotFound;
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<>(URI.create("component"),
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
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<>("domain");
        URI uri = URI.create("fabric3://runtime");
        domain = new LogicalCompositeComponent(uri, definition, null);
    }
}
