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
package org.fabric3.spi.domain;

import java.net.URI;
import javax.xml.namespace.QName;

/**
 * Implementations receive callbacks for events emitted by the application domain.
 */
public interface DeployListener {

    /**
     * Called when the contents of a contribution are deployed to the domain. This will be called before {@link #onDeploy(QName, String)} if the
     * contribution contains deployables.
     *
     * @param uri the contribution URI
     */
    void onDeploy(URI uri);

    /**
     * Called when the contents of a contribution are finished being deployed to the domain. This will be called after {@link #onDeploy(QName,
     * String)} if the contribution contains deployables.
     *
     * @param uri the contribution URI
     */
    void onDeployCompleted(URI uri);

    /**
     * Called when the contents of a contribution are undeployed from the domain. This will be called before {@link #onUndeploy(QName)} if the
     * contribution contains deployables.
     *
     * @param uri the contribution URI
     */
    void onUnDeploy(URI uri);

    /**
     * Called when the contents of a contribution have been undeployed from the domain. This will be called after {@link #onUndeploy(QName)} if the
     * contribution contains deployables.
     *
     * @param uri the contribution URI
     */
    void onUnDeployCompleted(URI uri);

    /**
     * Called when a composite is deployed to the domain.
     *
     * @param deployable the composite qualified name
     * @param plan       the deployment plan or null if none is specified
     */
    void onDeploy(QName deployable, String plan);

    /**
     * Called when a composite has been deployed to the domain.
     *
     * @param deployable the composite qualified name
     * @param plan       the deployment plan or null if none is specified
     */
    void onDeployCompleted(QName deployable, String plan);

    /**
     * Called when a composite is undeployed from the domain.
     *
     * @param undeployed the composite qualified name
     */
    void onUndeploy(QName undeployed);

    /**
     * Called when a composite is undeployed from the domain.
     *
     * @param undeployed the composite qualified name
     */
    void onUndeployCompleted(QName undeployed);

}
