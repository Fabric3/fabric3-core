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
package org.fabric3.api.host.contribution;

import java.net.URI;
import java.net.URL;

import org.fabric3.api.host.stream.Source;

/**
 * Represents a source artifact that will be contributed to a domain or an updated version of an existing contribution.
 */
public interface ContributionSource {

    /**
     * Returns the identifier for this contribution.
     *
     * @return the identifier for this contribution
     */
    URI getUri();

    /**
     * Returns true if the source should be persisted.
     *
     * @return true if the source should be persisted
     */
    boolean persist();

    /**
     * Returns true if the contribution is an extension.
     *
     * @return true if the contribution is an extension.
     */
    boolean isExtension();

    /**
     * Returns a {@link Source} for reading the contents of the contribution.
     *
     * @return the source
     */
    Source getSource();

    /**
     * Returns the contribution location in the form of a URL. Null may be returned if the contribution source is synthetic and not a physical
     * artifact.
     *
     * @return the URL or null
     */
    URL getLocation();

    /**
     * Returns the source timestamp.
     *
     * @return the source timestamp
     */
    long getTimestamp();

    /**
     * Returns the content type of the source.
     *
     * @return the content type of the source
     */
    String getContentType();
}