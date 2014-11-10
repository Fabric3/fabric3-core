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
package org.fabric3.api.host.domain;

import java.net.URI;
import java.util.List;

/**
 * The state of the domain encompassing the contributions that have been deployed and the deployable composites within those contributions that have been
 * deployed. Used during recovery to reconstitute the current domain state.
 */
public class DomainJournal {
    private List<URI> contributions;

    /**
     * Constructor.
     *
     * @param contributions the contributions deployed to the domain
     */
    public DomainJournal(List<URI> contributions) {
        this.contributions = contributions;
    }

    /**
     * Returns the contributions deployed to the domain.
     *
     * @return the contributions deployed to the domain
     */
    public List<URI> getContributions() {
        return contributions;
    }

}
