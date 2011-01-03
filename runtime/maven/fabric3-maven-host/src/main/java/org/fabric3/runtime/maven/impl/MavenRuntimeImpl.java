/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.runtime.maven.impl;

import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

import org.apache.maven.surefire.suite.SurefireTestSuite;

import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.repository.Repository;
import org.fabric3.host.repository.RepositoryException;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.runtime.maven.ContextStartException;
import org.fabric3.runtime.maven.MavenRuntime;
import org.fabric3.runtime.maven.ModuleContributionSource;
import org.fabric3.runtime.maven.TestSuiteFactory;
import org.fabric3.runtime.maven.repository.MavenRepository;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

import static org.fabric3.host.Names.APPLICATION_DOMAIN_URI;
import static org.fabric3.host.Names.CONTRIBUTION_SERVICE_URI;

/**
 * Default Maven runtime implementation.
 *
 * @version $Rev$ $Date$
 */
public class MavenRuntimeImpl extends AbstractRuntime implements MavenRuntime {
    private static final URI CONTRIBUTION_URI = URI.create("iTestContribution");

    public MavenRuntimeImpl(RuntimeConfiguration configuration) {
        super(configuration);
    }

    public void deploy(URL base, QName qName) throws ContributionException, DeploymentException {
        ModuleContributionSource source = new ModuleContributionSource(CONTRIBUTION_URI, base);
        // contribute the Maven project to the application domain
        ContributionService contributionService = getComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
        Domain domain = getComponent(Domain.class, APPLICATION_DOMAIN_URI);
        URI uri = contributionService.store(source);
        contributionService.install(uri);
        // activate the deployable composite in the domain
        domain.include(qName);
    }

    public void startContext(QName deployable) throws ContextStartException {
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(deployable);
        workContext.addCallFrame(frame);
        try {
            getScopeContainer().startContext(workContext);
        } catch (ComponentException e) {
            throw new ContextStartException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public SurefireTestSuite createTestSuite() {
        TestSuiteFactory factory = getComponent(TestSuiteFactory.class, TestSuiteFactory.FACTORY_URI);
        return factory.createTestSuite();
    }

    @Override
    protected Repository createRepository() throws InitializationException {
        try {
            MavenRepository repository = new MavenRepository();
            repository.init();
            return repository;
        } catch (RepositoryException e) {
            throw new InitializationException(e);
        }
    }
}
