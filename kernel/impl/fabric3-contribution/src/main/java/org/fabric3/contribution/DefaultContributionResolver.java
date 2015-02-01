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
package org.fabric3.contribution;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.repository.ArtifactCache;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation of the <code>ContributionResolver</code> which attempts to resolve a contribution URI against the metadata store, artifact cache, or
 * by delegating to <code>ContributionResolverExtension</code>s respectively.
 */
@EagerInit
public class DefaultContributionResolver implements ContributionResolver {
    private MetaDataStore store;
    private ArtifactCache cache;

    @Constructor
    public DefaultContributionResolver(@Reference MetaDataStore store, @Reference ArtifactCache cache) {
        this.store = store;
        this.cache = cache;
    }

    public URL resolve(URI contributionUri) {
        Contribution contribution = store.find(contributionUri);
        if (contribution != null) {
            return contribution.getLocation();
        }

        URL url = cache.get(contributionUri);
        if (url != null) {
            return url;
        }

        throw new Fabric3Exception("Contribution not found: " + contributionUri);
    }

    public List<URL> resolveAllLocations(URI contributionUri) {
        Contribution contribution = store.find(contributionUri);
        if (contribution != null) {
            List<URL> locations = new ArrayList<>();
            locations.add(contribution.getLocation());
            locations.addAll(contribution.getAdditionalLocations());
            return locations;
        }

        URL url = cache.get(contributionUri);
        if (url != null) {
            return Collections.singletonList(url);
        }

        throw new Fabric3Exception("Contribution not found: " + contributionUri);
    }


}
