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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.webapp;

import java.net.URI;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionListener;
import javax.xml.namespace.QName;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.runtime.Fabric3Runtime;

/**
 * The contract for artifacts loaded in the web application classloader to comminicate with the Fabric3 runtime loaded in a child classloader. For
 * example, filters and listeners may use this interface to notify the runtime of the web container events.
 *
 * @version $Rev$ $Date$
 */
public interface WebappRuntime extends ServletRequestListener, HttpSessionListener, Fabric3Runtime {

    /**
     * Deploys a composite in the domain.
     *
     * @param qName       the composite qualified name
     * @param componentId the id of the component that should be bound to the webapp
     * @throws DeploymentException   if there was a problem initializing the composite
     * @throws ContributionException if an error is found in the contribution. If validation errors are encountered, a ValidationException will be
     *                               thrown.
     */
    void deploy(QName qName, URI componentId) throws ContributionException, DeploymentException;

    /**
     * Returns the ServletRequestInjector for the runtime.
     *
     * @return the ServletRequestInjector for the runtime
     */
    ServletRequestInjector getRequestInjector();

}
