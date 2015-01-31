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
package org.fabric3.spi.contribution;

import java.net.URL;

import org.fabric3.api.host.ContainerException;

/**
 * Resolver for content type based on file extensions.
 */
public interface ContentTypeResolver {

    /**
     * Gets the content type for the contribution source.
     *
     * @param contentUrl URL for the content.
     * @return Content type for the contribution source or null if the content type is unknown.
     * @throws ContainerException if unable to resolve content type
     */
    String getContentType(URL contentUrl) throws ContainerException;

    /**
     * Gets the content type for the contribution source.
     *
     * @param fileName the content file name
     * @return Content type for the contribution source or null if the content type is unknown
     * @throws ContainerException if unable to resolve content type
     */
    String getContentType(String fileName) throws ContainerException;

    /**
     * Register a new file extension to content type mapping.
     *
     * @param fileExtension the file extension
     * @param contentType   the content type
     */
    void register(String fileExtension, String contentType);

    /**
     * unregister an existing file extension.
     *
     * @param fileExtension the file extension
     */
    void unregister(String fileExtension);

}
