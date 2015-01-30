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
package org.fabric3.api.host.contribution;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

/**
 * Thrown when an attempt is made to uninstall a contribution that is referenced by deployed components.
 */
public class ContributionLockedException extends UninstallException {
    private static final long serialVersionUID = -5443601943113359365L;
    private URI uri;
    private List<QName> deployables;

    public ContributionLockedException(String message, URI uri, List<QName> deployables) {
        super(message);
        this.uri = uri;
        this.deployables = deployables;
    }

    public URI getUri() {
        return uri;
    }

    public List<QName> getDeployables() {
        return deployables;
    }

}
