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
 */
package org.fabric3.management.rest.framework.domain.contribution;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

import org.fabric3.management.rest.model.Resource;

/**
 * A contribution in the domain.
 */
public class ContributionResource extends Resource {
    private static final long serialVersionUID = -6091215638459429825L;
    private URI uri;
    private String state;
    private List<QName> deployables;

    /**
     * Constructor.
     *
     * @param uri         the contribution URI
     * @param state       the contribution state
     * @param deployables the qualified names of deployable composites contained in the contribution
     */
    public ContributionResource(URI uri, String state, List<QName> deployables) {
        this.uri = uri;
        this.state = state;
        this.deployables = deployables;
    }

    /**
     * Returns the contribution URI.
     *
     * @return the contribution URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the contribution state.
     *
     * @return the contribution state
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the qualified names of deployable composites contained in the contribution
     *
     * @return the qualified names of deployable composites contained in the contribution
     */
    public List<QName> getDeployables() {
        return deployables;
    }

}
