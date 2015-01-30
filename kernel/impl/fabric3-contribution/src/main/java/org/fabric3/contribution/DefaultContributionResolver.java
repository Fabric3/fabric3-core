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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.contribution.ContributionResolverExtension;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResolutionException;
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
    private List<ContributionResolverExtension> extensions;

    @Constructor
    public DefaultContributionResolver(@Reference MetaDataStore store, @Reference ArtifactCache cache) {
        this.store = store;
        this.cache = cache;
        extensions = Collections.emptyList();
    }

    @Reference(required = false)
    public void setExtensions(List<ContributionResolverExtension> extensions) {
        this.extensions = extensions;
    }

    public URL resolve(URI contributionUri) throws ResolutionException {
        Contribution contribution = store.find(contributionUri);
        if (contribution != null) {
            return contribution.getLocation();
        }

        URL url = cache.get(contributionUri);
        if (url != null) {
            return url;
        }

        for (ContributionResolverExtension extension : extensions) {
            // provision and cache the contribution
            InputStream stream = extension.resolve(contributionUri);
            if (stream != null) {
                try {
                    return cache.cache(contributionUri, stream);
                } catch (ContainerException e) {
                    throw new ResolutionException("Error resolving contribution: " + contributionUri, e);
                }
            }
        }
        throw new ResolutionException("Contribution not found: " + contributionUri);
    }

    public List<URL> resolveAllLocations(URI contributionUri) throws ResolutionException {
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

        for (ContributionResolverExtension extension : extensions) {
            // provision and cache the contribution
            InputStream stream = extension.resolve(contributionUri);
            if (stream != null) {
                try {
                    return Collections.singletonList(cache.cache(contributionUri, stream));
                } catch (ContainerException e) {
                    throw new ResolutionException("Error resolving contribution: " + contributionUri, e);
                }
            }
        }
        throw new ResolutionException("Contribution not found: " + contributionUri);
    }

    public void release(URI contributionUri) throws ResolutionException {
        try {
            cache.remove(contributionUri);
        } catch (ContainerException e) {
            throw new ResolutionException("Error releasing artifact: " + contributionUri, e);
        }
    }

}
