/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.federation.contribution.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionUriResolver;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.artifact.ArtifactCache;
import org.fabric3.spi.artifact.CacheException;

/**
 * Resolves contributions using the <code>http</code> scheme, copying them to a local archive store.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class HttpContributionUriResolver implements ContributionUriResolver {
    private static final String HTTP_SCHEME = "http";

    private ArtifactCache cache;
    private MetaDataStore metaDataStore;

    public HttpContributionUriResolver(@Reference ArtifactCache cache, @Reference MetaDataStore store) {
        this.cache = cache;
        this.metaDataStore = store;
    }

    public URI decode(URI uri) {
        if (!HTTP_SCHEME.equals(uri.getScheme())) {
            // the contribution is being provisioned locally
            return uri;
        }
        return URI.create(uri.getPath().substring(HttpProvisionConstants.REPOSITORY.length() + 2)); // +2 for leading and trailing '/'
    }

    public URL resolve(URI uri) throws ResolutionException {
        if (!HTTP_SCHEME.equals(uri.getScheme())) {
            // the contribution is being provisioned locally, resolve it directly
            Contribution contribution = metaDataStore.find(uri);
            if (contribution == null) {
                throw new ResolutionException("Contribution not found: " + uri);
            }
            return contribution.getLocation();
        }
        InputStream stream;
        try {
            URI decoded = URI.create(uri.getPath().substring(HttpProvisionConstants.REPOSITORY.length() + 2)); // +2 for leading and trailing '/'
            // check to see if the archive is cached locally
            URL localURL = cache.get(decoded);
            if (localURL == null) {
                // resolve remotely
                URL url = uri.toURL();
                stream = url.openStream();
                localURL = cache.cache(decoded, stream);
            }
            return localURL;
        } catch (IOException e) {
            throw new ResolutionException("Error resolving artifact: " + uri, e);
        } catch (CacheException e) {
            throw new ResolutionException("Error resolving artifact: " + uri, e);
        }
    }

    public void release(URI uri) throws ResolutionException {
        try {
            cache.release(uri);
        } catch (CacheException e) {
            throw new ResolutionException("Error releasing artifact: " + uri, e);
        }
    }

    public int getInUseCount(URI uri) {
        return cache.getCount(uri);
    }
}

