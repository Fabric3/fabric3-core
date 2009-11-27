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
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 * Returns the contents of a contribution associated with the encoded servlet path. The servlet path corresponds to the contribution URI.
 *
 * @version $Rev$ $Date$
 */
public class ArchiveResolverServlet extends HttpServlet {
    private static final long serialVersionUID = -5822568715938454572L;
    private MetaDataStore store;

    public ArchiveResolverServlet(MetaDataStore store) {
        this.store = store;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String info = req.getPathInfo().substring(1);    // path info always begins with '/'
        try {
            URI uri = new URI(info);
            Contribution contribution = store.find(uri);
            if (contribution == null) {
                throw new ServletException("Contribution not found: " + info + ". Request URL was: " + info);
            }
            URL url = contribution.getLocation();
            copy(url.openStream(), resp.getOutputStream());
        } catch (URISyntaxException e) {
            throw new ServletException("Invalid URI: " + info, e);
        }
    }


    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
