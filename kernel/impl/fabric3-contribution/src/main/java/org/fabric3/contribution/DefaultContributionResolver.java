/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.contribution;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.artifact.ArtifactCache;
import org.fabric3.spi.artifact.CacheException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.contribution.ContributionResolverExtension;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResolutionException;

/**
 * Default implementation of the <code>ContributionResolver</code> which attempts to resolves a contribution URI against the metadata store, artifact
 * cache, or by delegating to <code>ContributionResolverExtension</code>s respectively.
 *
 * @version $Rev$ $Date$
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
                } catch (CacheException e) {
                    throw new ResolutionException("Error resolving contribution: " + contributionUri, e);
                }
            }
        }
        throw new ResolutionException("Contribution not found: " + contributionUri);
    }

    public void release(URI contributionUri) throws ResolutionException {
        try {
            cache.remove(contributionUri);
        } catch (CacheException e) {
            throw new ResolutionException("Error releasing artifact: " + contributionUri, e);
        }
    }

}
