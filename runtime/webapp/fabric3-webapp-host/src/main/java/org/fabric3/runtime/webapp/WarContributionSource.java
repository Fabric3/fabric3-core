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
package org.fabric3.runtime.webapp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.stream.Source;

/**
 * Used to install the current webapp as a contribution.
 *
 * @version $Rev$ $Date$
 */
public class WarContributionSource implements ContributionSource {

    private static final String CONTENT_TYPE = "application/vnd.fabric3.war";

    private URI contributionUri;
    private URL url;
    private long timestamp;

    public WarContributionSource(URI contributionUri) throws MalformedURLException {
        this.contributionUri = contributionUri;
        this.url = new File("/").toURI().toURL();
        timestamp = System.currentTimeMillis();
    }

    public boolean persist() {
        return false;
    }

    public URI getUri() {
        return contributionUri;
    }

    public Source getSource() {
        return null;
    }

    public URL getLocation() {
        return url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getContentType() {
        return CONTENT_TYPE;
    }
}
