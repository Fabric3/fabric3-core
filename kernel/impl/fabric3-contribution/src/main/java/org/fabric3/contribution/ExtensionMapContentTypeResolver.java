/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.oasisopen.sca.annotation.Init;

import org.fabric3.spi.contribution.ContentTypeResolutionException;
import org.fabric3.spi.contribution.ContentTypeResolver;

/**
 * Content type resolver that is implemented using a configured map.
 *
 * @version $Revision$ $Date$
 */
public class ExtensionMapContentTypeResolver implements ContentTypeResolver {

    // file extension to content type map
    private Map<String, String> extensionMap = new HashMap<String, String>();


    @Init
    public void init() {
        extensionMap.put("xml","application/xml");
        extensionMap.put("composite","text/vnd.fabric3.composite+xml");
        extensionMap.put("zip","application/zip");
        extensionMap.put("jar","application/zip");
        extensionMap.put("definitions","text/vnd.fabric3.definitions+xml");
        extensionMap.put("wsdl","text/wsdl+xml");
        extensionMap.put("contribution","text/vnd.fabric3.contribution");
    }

    public String getContentType(URL contentUrl) throws ContentTypeResolutionException {

        if (contentUrl == null) {
            throw new IllegalArgumentException("Content URL cannot be null");
        }

        String urlString = contentUrl.toExternalForm();
        try {

            String contentType = getContentType(urlString);

            if (contentType == null) {
                URLConnection connection = contentUrl.openConnection();
                contentType = connection.getContentType();
            }

            if (contentType == null ||  "application/octet-stream".equals(contentType)) {
                return null;
            }

            return contentType;
        } catch (IOException ex) {
            throw new ContentTypeResolutionException("Unable to resolve content type: " + urlString, urlString, ex);
        }

    }

    public void register(String fileExtension, String contentType) {
        extensionMap.put(fileExtension, contentType);
    }

    public void unregister(String fileExtension) {
        extensionMap.remove(fileExtension);
    }

    private String getContentType(String pathURI) {
        int extensionIndex = pathURI.lastIndexOf('.');
        if (extensionIndex != -1) {
            String extension = pathURI.substring(extensionIndex + 1);
            String contentType = extensionMap.get(extension);
            if (contentType != null) {
                return contentType;
            }
        }

        return null;
    }


}
