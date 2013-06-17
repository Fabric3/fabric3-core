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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.maven;

import java.net.URL;
import javax.xml.namespace.QName;

import org.apache.maven.surefire.suite.SurefireTestSuite;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;

/**
 * API for the Maven runtime. The Maven runtime requires system component of type Map<String, Wire> named "TestWireHolder" that contains wires to
 * integration test operations to be invoked. Extensions such as JUnit are required to introspect test implementation and generate the appropriate
 * metadata to instantiate wires to test operations. These wires must then be attached to the TestWireHolder component.
 */
public interface MavenRuntime extends Fabric3Runtime {

    /**
     * Deploys a composite by qualified name contained in the Maven module the runtime is currently executing for.
     *
     * @param base      the module output directory location
     * @param composite the composite qname to activate
     * @throws ContributionException if a contribution is thrown. The cause may a ValidationException resulting from  errors in the contribution. In
     *                               this case the errors should be reported back to the user.
     * @throws DeploymentException   if there is an error activating the test composite
     */
    void deploy(URL base, QName composite) throws ContributionException, DeploymentException;

    /**
     * Starts a component context.
     *
     * @param compositeId the context id
     * @throws ContextStartException if an error starting the context is encountered
     */
    void startContext(QName compositeId) throws ContextStartException;

    /**
     * Creates a test suite for testing components in the deployed composite.
     *
     * @return the test suite
     */
    SurefireTestSuite createTestSuite();

    /**
     * Returns the host info.
     *
     * @return the host info
     */
    HostInfo getHostInfo();
}
