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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.contribution.ContributionUriEncoder;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.host.ServletHost;

/**
 * Encodes a contribution URI so it can be dereferenced in a domain via HTTP. The encoding maps from the contribution URI to an HTTP-based URI.
 *
 * @version $Rev$ $Date$
 */
public class HTTPContributionUriEncoder implements ContributionUriEncoder {
    private ServletHost host;
    private MetaDataStore store;
    private String address;
    private String mappingPath = HttpProvisionConstants.REPOSITORY;

    public HTTPContributionUriEncoder(@Reference ServletHost host, @Reference MetaDataStore store) {
        this.host = host;
        this.store = store;
    }

    @Property
    public void setMappingPath(String path) {
        mappingPath = path;
    }

    @Property
    public void setAddress(String address) {
        this.address = address;
    }

    @Init
    public void init() throws UnknownHostException {
        if (address == null) {
            address = InetAddress.getLocalHost().getHostAddress();
        }
        host.registerMapping("/" + mappingPath + "/*", new ArchiveResolverServlet(store));
    }

    public URI encode(URI uri) throws URISyntaxException {
        String path = "/" + mappingPath + "/" + uri.getPath();
        return new URI("http", null, address, host.getHttpPort(), path, null, null);
    }
}
