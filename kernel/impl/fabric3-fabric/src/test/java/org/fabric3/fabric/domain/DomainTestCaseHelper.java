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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.Collections;
import javax.xml.namespace.QName;

import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Rev: 10127 $ $Date: 2011-03-27 10:41:17 -0700 (Sun, 27 Mar 2011) $
 */
public class DomainTestCaseHelper {
    private static final URI COMPONENT_URI = URI.create("fabric3://domain/component");
    private static final URI CONTRIBUTION_URI = URI.create("contribution");

    private static final QName DEPLOYABLE = new QName("foo", "bar");


    public static IAnswer<InstantiationContext> createAnswer(final ComponentDefinition definition) {
        return new IAnswer<InstantiationContext>() {

            @SuppressWarnings({"unchecked"})
            public InstantiationContext answer() throws Throwable {
                LogicalCompositeComponent domainComposite = (LogicalCompositeComponent) EasyMock.getCurrentArguments()[1];

                LogicalComponent logicalComponent = new LogicalComponent(COMPONENT_URI, definition, domainComposite);
                logicalComponent.setDeployable(DEPLOYABLE);
                domainComposite.addComponent(logicalComponent);
                return new InstantiationContext();
            }
        };
    }

    public static IAnswer<InstantiationContext> createErrorAnswer(final ComponentDefinition definition) {
        return new IAnswer<InstantiationContext>() {

            @SuppressWarnings({"unchecked"})
            public InstantiationContext answer() throws Throwable {
                LogicalCompositeComponent domainComposite = (LogicalCompositeComponent) EasyMock.getCurrentArguments()[1];

                LogicalComponent logicalComponent = new LogicalComponent(COMPONENT_URI, definition, domainComposite);
                logicalComponent.setDeployable(DEPLOYABLE);
                domainComposite.addComponent(logicalComponent);
                InstantiationContext context = new InstantiationContext();
                context.addError(new AssemblyFailure(COMPONENT_URI, CONTRIBUTION_URI, Collections.emptyList()) {
                });
                return context;
            }
        };
    }

    @SuppressWarnings({"unchecked"})
    public static Composite createComposite(Contribution contribution, ComponentDefinition definition, MetaDataStore store) {
        Composite composite = new Composite(DEPLOYABLE);
        composite.add(definition);
        composite.setContributionUri(contribution.getUri());

        QNameSymbol symbol = new QNameSymbol(DEPLOYABLE);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol, composite);
        Resource resource = new Resource(contribution, null, "jar");
        element.setResource(resource);

        Deployable deployable = new Deployable(DEPLOYABLE);
        contribution.getManifest().addDeployable(deployable);
        EasyMock.expect(store.find(Composite.class, symbol)).andReturn(element).anyTimes();
        return composite;
    }

    public static Contribution createContribution(MetaDataStore store) {
        Contribution contribution = new Contribution(CONTRIBUTION_URI);
        contribution.setState(ContributionState.INSTALLED);

        EasyMock.expect(store.find(CONTRIBUTION_URI)).andReturn(contribution).anyTimes();
        return contribution;
    }


}
