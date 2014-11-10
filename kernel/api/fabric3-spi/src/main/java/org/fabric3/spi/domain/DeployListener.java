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
package org.fabric3.spi.domain;

import javax.xml.namespace.QName;
import java.net.URI;

/**
 * Implementations receive callbacks for events emitted by the application domain.
 */
public interface DeployListener {

    /**
     * Called when the contents of a contribution are deployed to the domain. This will be called before {@link #onDeploy(QName)} if the
     * contribution contains deployables.
     *
     * @param uri the contribution URI
     */
    void onDeploy(URI uri);

    /**
     * Called when the contents of a contribution are finished being deployed to the domain. This will be called after {@link #onDeploy(QName)} if the contribution contains deployables.
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
     *  @param deployable the composite qualified name
     *
     */
    void onDeploy(QName deployable);

    /**
     * Called when a composite has been deployed to the domain.
     *  @param deployable the composite qualified name
     *
     */
    void onDeployCompleted(QName deployable);

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
