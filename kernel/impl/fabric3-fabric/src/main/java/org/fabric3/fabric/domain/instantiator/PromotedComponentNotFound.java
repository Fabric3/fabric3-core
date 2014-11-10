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
import org.fabric3.spi.model.instance.Bindable;

public class PromotedComponentNotFound extends AssemblyFailure {
    private URI promotedComponentUri;
    private URI bindableUri;

    public PromotedComponentNotFound(Bindable bindable, URI promotedComponentUri) {
        super(bindable.getParent().getUri(), bindable.getParent().getDefinition().getContributionUri(), Collections.singletonList(bindable));
        this.bindableUri = bindable.getUri();
        this.promotedComponentUri = promotedComponentUri;
    }

    public String getMessage() {
        return "The component " + promotedComponentUri + " promoted by " + bindableUri + " was not found";
    }
}
