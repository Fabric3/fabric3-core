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

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;

/**
 * A contribution import.
 */
public interface Import extends Serializable {

    /**
     * Returns the export type.
     *
     * @return the export type
     */
    QName getType();

    /**
     * A URI representing the import artifact location.
     *
     * @return a URI representing the import artifact location or null if no location was specified
     */
    URI getLocation();

    /**
     * Returns true if this import supports wiring to multiple exports.
     *
     * @return true if this import supports wiring to multiple exports.
     */
    boolean isMultiplicity();

    /**
     * True if this import must be resolved.
     *
     * @return true if this import must be resolved.
     */
    boolean isRequired();

    /**
     * Returns the collection of resolved exports for this import. The key corresponds to the exporting contribution URI and the value is the export
     * that satisfies the import.
     *
     * @return the collection of resolved exports for this import
     */
    Map<URI, Export> getResolved();

    /**
     * Adds an export that satisfies the current import.
     *
     * @param contributionUri the exporting contribution URI
     * @param export          the export
     */
    void addResolved(URI contributionUri, Export export);

}
