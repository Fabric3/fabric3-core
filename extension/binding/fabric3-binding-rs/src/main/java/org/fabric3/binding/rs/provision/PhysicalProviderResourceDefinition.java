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
package org.fabric3.binding.rs.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalResourceDefinition;

/**
 *
 */
public class PhysicalProviderResourceDefinition extends PhysicalResourceDefinition {
    private static final long serialVersionUID = 122854501779816160L;

    private URI providerUri;
    private String bindingAnnotation;
    private URI contributionUri;
    private String providerClass;

    public PhysicalProviderResourceDefinition(URI providerUri, String bindingAnnotation, String providerClass, URI contributionUri) {
        this.providerUri = providerUri;
        this.bindingAnnotation = bindingAnnotation;
        this.providerClass = providerClass;
        this.contributionUri = contributionUri;
    }

    public URI getProviderUri() {
        return providerUri;
    }

    public String getBindingAnnotation() {
        return bindingAnnotation;
    }

    public String getProviderClass() {
        return providerClass;
    }

    public URI getContributionUri() {
        return contributionUri;
    }

}
