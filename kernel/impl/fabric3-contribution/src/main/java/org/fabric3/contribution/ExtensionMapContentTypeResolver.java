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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.oasisopen.sca.annotation.Init;

/**
 * Content type resolver that is implemented using a configured map.
 */
public class ExtensionMapContentTypeResolver implements ContentTypeResolver {

    // file extension to content type map
    private Map<String, String> extensionMap = new HashMap<>();

    @Init
    public void init() {
        extensionMap.put("xml", "application/xml");
        extensionMap.put("composite", "text/vnd.fabric3.composite+xml");
        extensionMap.put("zip", "application/zip");
        extensionMap.put("jar", "application/zip");
        extensionMap.put("definitions", "text/vnd.fabric3.definitions+xml");
        extensionMap.put("wsdl", "text/wsdl+xml");
        extensionMap.put("contribution", "text/vnd.fabric3.contribution");
    }

    public String getContentType(URL contentUrl) throws Fabric3Exception {

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

            if (contentType == null || "application/octet-stream".equals(contentType)) {
                return null;
            }

            return contentType;
        } catch (IOException ex) {
            throw new Fabric3Exception("Unable to resolve content type: " + urlString, ex);
        }
    }

    public String getContentType(String pathURI) {
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

    public void register(String fileExtension, String contentType) {
        extensionMap.put(fileExtension, contentType);
    }

    public void unregister(String fileExtension) {
        extensionMap.remove(fileExtension);
    }

}
