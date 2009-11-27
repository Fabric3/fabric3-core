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
package org.fabric3.host.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Represents a source artifact that will be contributed to a domain or an updated version of an existing contribution.
 *
 * @version $Rev$ $Date$
 */
public interface ContributionSource {

    /**
     * Returns true if the source shoud be persisted.
     *
     * @return true if the source shoud be persisted
     */
    boolean persist();

    /**
     * Returns the identifier for this contribution or null if one has not been assigned (i.e. it is a new contribution and not an update).
     *
     * @return the identifier for this contribution
     */
    URI getUri();

    /**
     * Returns a input stream for the source.
     *
     * @return a input stream for the source
     * @throws IOException if an error occurs returning the stream
     */
    InputStream getSource() throws IOException;

    /**
     * If the source is local, returns the source URL
     *
     * @return the source URL
     */
    URL getLocation();

    /**
     * Returns the source timestamp.
     *
     * @return the source timestamp
     */
    long getTimestamp();

    /**
     * Returns the source checksum.
     *
     * @return the source checksum
     */
    byte[] getChecksum();


    /**
     * Returns the content type of the source.
     *
     * @return the content type of the source
     */
    String getContentType();
}