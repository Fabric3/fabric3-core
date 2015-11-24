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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Maintains ordering for contributions based on manifest information.
 *
 * Contributions are divided into three categories, boot, base and isolated. Boot contributions are loaded prior to other contributions. Base contributions only
 * rely on boot runtime capabilities. Isolated contributions may rely on capabilities provided by other extensions and must be deployed after the base
 * extensions.
 */
public class ContributionOrder {
    private List<URI> bootstrap = new ArrayList<>();
    private List<URI> base = new ArrayList<>();
    private List<URI> isolated = new ArrayList<>();

    public List<URI> getBootstrapContributions() {
        return bootstrap;
    }

    public void addBootstrapContribution(URI uri) {
        bootstrap.add(uri);
    }

    public List<URI> getBaseContributions() {
        return base;
    }

    public void addBaseContribution(URI uri) {
        base.add(uri);
    }

    public List<URI> getIsolatedContributions() {
        return isolated;
    }

    public void addIsolatedContribution(URI uri) {
        isolated.add(uri);
    }
}