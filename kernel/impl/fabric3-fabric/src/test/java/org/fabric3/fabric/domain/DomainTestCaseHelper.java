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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain;

import javax.xml.namespace.QName;
import java.net.URI;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 *
 */
public class DomainTestCaseHelper {
    private static final URI COMPONENT_URI = URI.create("fabric3://domain/component");
    private static final URI CONTRIBUTION_URI = URI.create("contribution");

    private static final QName DEPLOYABLE = new QName("foo", "bar");


    @SuppressWarnings("unchecked")
    public static IAnswer<InstantiationContext> createAnswer(final Component definition) {
        return () -> {
            LogicalCompositeComponent domainComposite = (LogicalCompositeComponent) EasyMock.getCurrentArguments()[1];

            LogicalComponent logicalComponent = new LogicalComponent(COMPONENT_URI, definition, domainComposite);
            definition.setContributionUri(CONTRIBUTION_URI);
            domainComposite.addComponent(logicalComponent);
            return new InstantiationContext();
        };
    }

    @SuppressWarnings({"unchecked"})
    public static Composite createComposite(Contribution contribution, Component definition, MetaDataStore store) {
        Composite composite = new Composite(DEPLOYABLE);
        composite.add(definition);
        composite.setContributionUri(contribution.getUri());

        QNameSymbol symbol = new QNameSymbol(DEPLOYABLE);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol, composite);
        Resource resource = new Resource(contribution, null, "jar");
        element.setResource(resource);

        Deployable deployable = new Deployable(DEPLOYABLE);
        contribution.getManifest().addDeployable(deployable);
        EasyMock.expect(store.find(Composite.class, symbol)).andReturn(element).anyTimes();
        return composite;
    }

    public static Contribution createContribution(MetaDataStore store) {
        Contribution contribution = new Contribution(CONTRIBUTION_URI);
        contribution.install();

        EasyMock.expect(store.find(CONTRIBUTION_URI)).andReturn(contribution).anyTimes();
        return contribution;
    }


}
