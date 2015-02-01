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
package org.fabric3.fabric.domain;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;

/**
 * Provides utility functions for working with contributions.
 */
public interface ContributionHelper {

    /**
     * Returns the list of deployable composites contained in the list of contributions that are configured to run in the current runtime mode.
     *
     * @param contributions the contributions containing the deployables
     * @return the list of deployables
     */
    List<Composite> getDeployables(Set<Contribution> contributions);

    /**
     * Finds a deployable by name.
     *
     * @param deployable the deployable name
     * @return the deployable
     * @throws ContainerException if the deployable cannot be resolved
     */
    Composite findComposite(QName deployable) throws ContainerException;

    /**
     * Resolves the contributions from the list of URIs.
     *
     * @param uris the contribution  URIs
     * @return the set of contributions
     */
    Set<Contribution> findContributions(List<URI> uris);

    /**
     * Locks a set of contributions. The lock owners are the deployables in the contribution.
     *
     * @param contributions the contributions
     * @throws ContainerException if a deployable is already deployed
     */
    void lock(Set<Contribution> contributions) throws ContainerException;

    /**
     * Releases locks held on a set of contributions.
     *
     * @param contributions the contributions to release locks on
     */
    void releaseLocks(Set<Contribution> contributions);

}
