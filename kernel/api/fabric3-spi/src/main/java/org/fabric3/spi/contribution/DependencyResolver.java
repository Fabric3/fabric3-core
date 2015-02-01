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
package org.fabric3.spi.contribution;

import java.util.List;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Resolves contribution dependencies.
 */
public interface DependencyResolver {

    /**
     * Resolves dependencies for the given contributions. An ordered list is returned based on a reverse topological sort of contribution resolved
     * imports and capability requirements.
     *
     * @param contributions the  list of contributions to order
     * @return the ordered list of contributions
     * @throws Fabric3Exception if an error occurs ordering the contributions such as an unresolvable import or dependency cycle
     */
    List<Contribution> resolve(List<Contribution> contributions) throws Fabric3Exception;

    /**
     * Orders a list of contributions to uninstall. Ordering is calculated by topologically sorting the list based on resolved contribution imports
     * and capability requirements.
     *
     * @param contributions the contributions to order
     * @return the ordered list of contributions
     */
    List<Contribution> orderForUninstall(List<Contribution> contributions);

}
