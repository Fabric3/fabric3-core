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
package org.fabric3.fabric.domain.instantiator;

import java.net.URI;
import java.util.Collections;

import org.fabric3.api.host.failure.AssemblyFailure;
import org.fabric3.spi.model.instance.LogicalReference;

public class AmbiguousReference extends AssemblyFailure {
    private URI referenceUri;
    private URI promotedComponentUri;

    /**
     * Constructor.
     *
     * @param source               the reference source
     * @param promotedComponentUri the promoted component URI.
     */
    public AmbiguousReference(LogicalReference source, URI promotedComponentUri) {
        super(source.getParent().getUri(), source.getParent().getDefinition().getContributionUri(), Collections.singletonList(source));
        this.referenceUri = source.getUri();
        this.promotedComponentUri = promotedComponentUri;
    }

    public String getMessage() {
        return "The promoted reference " + referenceUri + " must explicitly specify the reference it is promoting on component "
                + promotedComponentUri + " as the component has more than one reference";
    }
}
